package com.easycomic.core.util

import android.content.Context
import android.net.Uri
import com.easycomic.core.database.Manga
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicFileParser @Inject constructor(
    private val context: Context
) {
    private val tempFiles = mutableListOf<File>()
    
    init {
        // Register cleanup hook for application shutdown
        Runtime.getRuntime().addShutdownHook(Thread {
            cleanupTempFiles()
        })
    }
    
    suspend fun parseComicFile(uri: Uri): Result<Manga> = withContext(Dispatchers.IO) {
        try {
            val filePath = getFilePathFromUri(uri)
            val file = File(filePath)
            
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found"))
            }
            
            val fileName = file.nameWithoutExtension
            val fileSize = file.length()
            val format = getFileFormat(file.extension)
            
            val pages = extractPages(file)
            val coverImage = getCoverImage(pages)
            
            val manga = Manga(
                title = fileName,
                filePath = filePath,
                coverImagePath = coverImage,
                totalPages = pages.size,
                format = format,
                fileSize = fileSize,
                currentPage = 0,
                isFavorite = false
            )
            
            Result.success(manga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getFilePathFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.${getFileExtension(uri)}")
        
        try {
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Add temp file to tracking list for cleanup
            tempFiles.add(tempFile)
            
            tempFile.absolutePath
        } catch (e: Exception) {
            // Clean up temp file if creation failed
            tempFile.delete()
            throw e
        }
    }
    
    private fun getFileExtension(uri: Uri): String {
        val path = uri.path ?: return ""
        val lastDot = path.lastIndexOf('.')
        return if (lastDot != -1) path.substring(lastDot + 1) else ""
    }
    
    private fun getFileFormat(extension: String): String {
        return when (extension.lowercase()) {
            "zip", "cbz" -> "ZIP"
            "rar", "cbr" -> "RAR"
            else -> "UNKNOWN"
        }
    }
    
    private suspend fun extractPages(file: File): List<String> = withContext(Dispatchers.IO) {
        val pages = mutableListOf<String>()
        
        when (file.extension.lowercase()) {
            "zip", "cbz" -> extractZipPages(file, pages)
            "rar", "cbr" -> extractRarPages(file, pages) // Implementation needed
            else -> throw Exception("Unsupported file format")
        }
        
        pages
    }
    
    private fun extractZipPages(file: File, pages: MutableList<String>) {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
                .asSequence()
                .filter { !it.isDirectory }
                .filter { isImageFile(it.name) }
                .sortedWith(compareBy { naturalSortKey(it.name) })
            
            for (entry in entries) {
                val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}_${entry.name}")
                try {
                    zipFile.getInputStream(entry).use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Add to tracking list
                    synchronized(tempFiles) {
                        tempFiles.add(tempFile)
                    }
                    pages.add(tempFile.absolutePath)
                } catch (e: Exception) {
                    // Clean up failed temp file
                    tempFile.delete()
                    throw e
                }
            }
        }
    }
    
    private fun extractRarPages(file: File, pages: MutableList<String>) {
        try {
            Archive(file).use { archive ->
                val fileHeaders = archive.fileHeaders
                    .filter { !it.isDirectory }
                    .filter { isImageFile(it.fileName) }
                    .sortedWith(compareBy { naturalSortKey(it.fileName) })
                
                for (header in fileHeaders) {
                    val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}_${header.fileName}")
                    try {
                        // Extract file from RAR
                        val outputStream = FileOutputStream(tempFile)
                        archive.extractFile(header, outputStream)
                        outputStream.close()
                        
                        // Add to tracking list
                        synchronized(tempFiles) {
                            tempFiles.add(tempFile)
                        }
                        pages.add(tempFile.absolutePath)
                        
                    } catch (e: Exception) {
                        // Clean up failed temp file
                        tempFile.delete()
                        throw e
                    }
                }
            }
        } catch (e: Exception) {
            throw Exception("Failed to extract RAR file: ${e.message}", e)
        }
    }
    
    private fun isImageFile(fileName: String): Boolean {
        val imageExtensions = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in imageExtensions
    }
    
    private fun naturalSortKey(fileName: String): String {
        // Extract numbers from filename for natural sorting
        val numbers = Regex("\\d+").findAll(fileName).map { it.value.toInt() }
        return fileName + numbers.joinToString("") { it.toString().padStart(10, '0') }
    }
    
    private suspend fun getCoverImage(pages: List<String>): String? = withContext(Dispatchers.IO) {
        if (pages.isEmpty()) return@withContext null
        
        // Try to find a cover image by common naming patterns
        val coverPatterns = listOf(
            "cover", "folder", "front", "000", "001", "00", "0"
        )
        
        for (pattern in coverPatterns) {
            val coverFile = pages.find { 
                File(it).nameWithoutExtension.contains(pattern, ignoreCase = true) 
            }
            if (coverFile != null) return@withContext coverFile
        }
        
        // If no cover found, use the first page
        pages.firstOrNull()
    }
    
    suspend fun extractPageImage(pagePath: String): ByteArray = withContext(Dispatchers.IO) {
        val file = File(pagePath)
        if (!file.exists()) {
            throw Exception("Page file not found")
        }
        
        file.readBytes()
    }
    
    suspend fun cleanupTempFiles() {
        withContext(Dispatchers.IO) {
            // Clean up tracked temp files
            synchronized(tempFiles) {
                val filesToClean = tempFiles.toList()
                tempFiles.clear()
                
                var cleanedCount = 0
                var totalSize = 0L
                
                filesToClean.forEach { file ->
                    if (file.exists()) {
                        try {
                            val fileSize = file.length()
                            if (file.delete()) {
                                cleanedCount++
                                totalSize += fileSize
                            }
                        } catch (e: SecurityException) {
                            // Log error but continue with other files
                            e.printStackTrace()
                        }
                    }
                }
                
                if (cleanedCount > 0) {
                    // Log cleanup statistics
                    android.util.Log.d("ComicFileParser", 
                        "Cleaned up $cleanedCount temp files, ${totalSize / 1024}KB freed")
                }
            }
            
            // Also scan for orphaned temp files (safety net)
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("temp_") || file.name.matches(Regex("\\d+_.+"))) {
                    try {
                        if (file.exists() && !tempFiles.contains(file)) {
                            file.delete()
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    /**
     * Clean up temp files older than specified age (in milliseconds)
     */
    suspend fun cleanupOldTempFiles(maxAgeMs: Long = 24 * 60 * 60 * 1000) { // 24 hours default
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            synchronized(tempFiles) {
                val iterator = tempFiles.iterator()
                var cleanedCount = 0
                
                while (iterator.hasNext()) {
                    val file = iterator.next()
                    if (file.exists()) {
                        val fileAge = currentTime - file.lastModified()
                        if (fileAge > maxAgeMs) {
                            try {
                                if (file.delete()) {
                                    iterator.remove()
                                    cleanedCount++
                                }
                            } catch (e: SecurityException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // Remove non-existent files from tracking
                        iterator.remove()
                    }
                }
                
                if (cleanedCount > 0) {
                    android.util.Log.d("ComicFileParser", 
                        "Cleaned up $cleanedCount old temp files (older than ${maxAgeMs / 3600000} hours)")
                }
            }
        }
    }
    
    /**
     * Get temporary file statistics
     */
    suspend fun getTempFileStats(): TempFileStats = withContext(Dispatchers.IO) {
        synchronized(tempFiles) {
            val existingFiles = tempFiles.filter { it.exists() }
            val totalSize = existingFiles.sumOf { it.length() }
            val oldestFile = existingFiles.minByOrNull { it.lastModified() }
            val newestFile = existingFiles.maxByOrNull { it.lastModified() }
            
            TempFileStats(
                fileCount = existingFiles.size,
                totalSizeBytes = totalSize,
                oldestFileTimestamp = oldestFile?.lastModified() ?: 0L,
                newestFileTimestamp = newestFile?.lastModified() ?: 0L,
                averageFileSizeBytes = if (existingFiles.isNotEmpty()) totalSize / existingFiles.size else 0L
            )
        }
    }
    
    /**
     * Data class for temp file statistics
     */
    data class TempFileStats(
        val fileCount: Int,
        val totalSizeBytes: Long,
        val oldestFileTimestamp: Long,
        val newestFileTimestamp: Long,
        val averageFileSizeBytes: Long
    )
}