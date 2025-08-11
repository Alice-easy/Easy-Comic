package com.easycomic.data.service

import android.content.Context
import android.net.Uri
import com.easycomic.data.model.ComicInfo
import com.easycomic.data.model.ImageData
import com.easycomic.data.parser.ComicParser
import com.easycomic.data.repository.MangaRepositoryImpl
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.data.util.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.ArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.io.FilenameUtils
import timber.log.Timber
import java.io.*
import java.util.zip.ZipEntry

/**
 * 漫画导入服务
 * 负责解析漫画文件并将其导入到数据库中
 */
class ComicImportService(
    private val context: Context,
    private val mangaRepositoryImpl: MangaRepositoryImpl,
    private val fileHelper: FileHelper
) {
    
    /**
     * 导入漫画文件
     * @param uri 文件URI
     * @return 导入结果流
     */
    fun importComic(uri: Uri): Flow<ImportResult> = flow {
        try {
            emit(ImportResult(status = ImportStatus.PROCESSING, progress = 0))
            
            // 验证文件
            val validationResult = validateComicFile(uri)
            if (!validationResult.isValid) {
                emit(ImportResult(
                    status = ImportStatus.FAILED,
                    error = validationResult.errorMessage
                ))
                return@flow
            }
            
            emit(ImportResult(status = ImportStatus.PARSING, progress = 10))
            
            // 解析漫画文件
            val comicInfo = parseComicFile(uri)
            if (comicInfo == null) {
                emit(ImportResult(
                    status = ImportStatus.FAILED,
                    error = "无法解析漫画文件"
                ))
                return@flow
            }
            
            emit(ImportResult(status = ImportStatus.EXTRACTING_COVER, progress = 50))
            
            // 提取封面
            val coverPath = extractCoverImage(uri, comicInfo)
            
            emit(ImportResult(status = ImportStatus.SAVING_TO_DATABASE, progress = 80))
            
            // 保存到数据库
            val manga = createMangaFromComicInfo(comicInfo, uri, coverPath)
            val mangaId = mangaRepositoryImpl.insertOrUpdateManga(manga)
            
            emit(ImportResult(
                status = ImportStatus.COMPLETED,
                progress = 100,
                mangaId = mangaId,
                manga = manga
            ))
            
        } catch (e: Exception) {
            Timber.e(e, "导入漫画文件失败")
            emit(ImportResult(
                status = ImportStatus.FAILED,
                error = "导入失败: ${e.message}"
            ))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 批量导入漫画文件
     * @param uris 文件URI列表
     * @return 导入结果流
     */
    fun importComics(uris: List<Uri>): Flow<BatchImportResult> = flow {
        val results = mutableListOf<ImportResult>()
        val total = uris.size
        
        uris.forEachIndexed { index, uri ->
            try {
                emit(BatchImportResult(
                    status = BatchImportStatus.PROCESSING,
                    currentIndex = index,
                    total = total,
                    currentFile = FileHelper.getFileName(context, uri)
                ))
                
                importComic(uri).collect { result ->
                    results.add(result)
                    emit(BatchImportResult(
                        status = BatchImportStatus.ITEM_COMPLETED,
                        currentIndex = index,
                        total = total,
                        currentFile = FileHelper.getFileName(context, uri),
                        currentItemResult = result
                    ))
                }
            } catch (e: Exception) {
                Timber.e(e, "导入漫画文件失败: ${FileHelper.getFileName(context, uri)}")
                results.add(ImportResult(
                    status = ImportStatus.FAILED,
                    error = "导入失败: ${e.message}"
                ))
            }
        }
        
        emit(BatchImportResult(
            status = BatchImportStatus.COMPLETED,
            total = total,
            results = results
        ))
    }.flowOn(Dispatchers.IO)
    
    /**
     * 验证漫画文件
     */
    private suspend fun validateComicFile(uri: Uri): ValidationResult = withContext(Dispatchers.IO) {
        try {
            val fileName = FileHelper.getFileName(context, uri)
            val fileExtension = FilenameUtils.getExtension(fileName).lowercase()
            
            // 检查文件扩展名
            if (!SUPPORTED_FORMATS.contains(fileExtension)) {
                return@withContext ValidationResult(
                    isValid = false,
                    errorMessage = "不支持的文件格式: $fileExtension"
                )
            }
            
            // 检查文件大小
            val fileSize = FileHelper.getFileSize(context, uri)
            if (fileSize > MAX_FILE_SIZE) {
                return@withContext ValidationResult(
                    isValid = false,
                    errorMessage = "文件过大: ${formatFileSize(fileSize)}"
                )
            }
            
            // 尝试打开文件流以验证文件完整性
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // 简单的文件完整性检查
                val buffer = ByteArray(1024)
                if (stream.read(buffer) == -1) {
                    return@withContext ValidationResult(
                        isValid = false,
                        errorMessage = "文件为空或损坏"
                    )
                }
            }
            
            ValidationResult(isValid = true)
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "文件验证失败: ${e.message}"
            )
        }
    }
    
    /**
     * 解析漫画文件
     */
    private suspend fun parseComicFile(uri: Uri): ComicInfo? = withContext(Dispatchers.IO) {
        try {
            val fileName = FileHelper.getFileName(context, uri)
            val fileExtension = FilenameUtils.getExtension(fileName).lowercase()
            val fileSize = FileHelper.getFileSize(context, uri)
            
            val comicInfo = ComicInfo(
                title = FilenameUtils.getBaseName(fileName),
                filePath = FileHelper.getFilePath(context, uri),
                fileUri = uri.toString(),
                fileFormat = fileExtension,
                fileSize = fileSize,
                dateAdded = System.currentTimeMillis()
            )
            
            // 根据文件格式解析
            when (fileExtension) {
                "zip", "cbz" -> parseZipFile(uri, comicInfo)
                "rar", "cbr" -> parseRarFile(uri, comicInfo)
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "解析漫画文件失败")
            null
        }
    }
    
    /**
     * 解析ZIP文件
     */
    private suspend fun parseZipFile(uri: Uri, comicInfo: ComicInfo): ComicInfo? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipArchiveInputStream(inputStream).use { zipInputStream ->
                    val entries = mutableListOf<ComicInfo.ComicEntry>()
                    var pageCount = 0
                    var coverEntry: ComicInfo.ComicEntry? = null
                    
                    var entry: ArchiveEntry? = zipInputStream.nextEntry
                    while (entry != null) {
                        val entryName = entry.name.lowercase()
                        
                        // 检查是否为图片文件
                        if (isImageFile(entryName)) {
                            val comicEntry = ComicInfo.ComicEntry(
                                name = entry.name,
                                path = entry.name,
                                size = entry.size,
                                compressedSize = entry.compressedSize,
                                lastModified = entry.lastModifiedDate.time,
                                index = pageCount
                            )
                            entries.add(comicEntry)
                            
                            // 寻找封面图片
                            if (coverEntry == null && isCoverImage(entryName)) {
                                coverEntry = comicEntry
                            }
                            
                            pageCount++
                        }
                        
                        entry = zipInputStream.nextEntry
                    }
                    
                    // 如果没有找到封面，使用第一张图片
                    if (coverEntry == null && entries.isNotEmpty()) {
                        coverEntry = entries.first()
                    }
                    
                    comicInfo.copy(
                        pageCount = pageCount,
                        entries = entries,
                        coverEntry = coverEntry
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "解析ZIP文件失败")
            null
        }
    }
    
    /**
     * 解析RAR文件
     */
    private suspend fun parseRarFile(uri: Uri, comicInfo: ComicInfo): ComicInfo? = withContext(Dispatchers.IO) {
        try {
            // RAR文件解析需要特殊的库
            // 这里先返回基本信息，实际实现需要集成RAR解析库
            comicInfo.copy(
                pageCount = 0, // 需要实际解析
                entries = emptyList(),
                coverEntry = null
            )
        } catch (e: Exception) {
            Timber.e(e, "解析RAR文件失败")
            null
        }
    }
    
    /**
     * 提取封面图片
     */
    private suspend fun extractCoverImage(uri: Uri, comicInfo: ComicInfo): String? = withContext(Dispatchers.IO) {
        try {
            val coverEntry = comicInfo.coverEntry
            if (coverEntry == null) {
                return@withContext null
            }
            
            // 创建封面文件路径
            val coverFileName = "cover_${comicInfo.title}_${System.currentTimeMillis()}.${getFileExtension(coverEntry.name)}"
            val coverFile = File(context.cacheDir, coverFileName)
            
            // 提取封面图片
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipArchiveInputStream(inputStream).use { zipInputStream ->
                    var entry: ArchiveEntry? = zipInputStream.nextEntry
                    while (entry != null) {
                        if (entry.name == coverEntry.name) {
                            FileOutputStream(coverFile).use { outputStream ->
                                zipInputStream.copyTo(outputStream)
                            }
                            break
                        }
                        entry = zipInputStream.nextEntry
                    }
                }
            }
            
            if (coverFile.exists()) {
                coverFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "提取封面图片失败")
            null
        }
    }
    
    /**
     * 从ComicInfo创建Manga对象
     */
    private fun createMangaFromComicInfo(comicInfo: ComicInfo, uri: Uri, coverPath: String?): Manga {
        return Manga(
            title = comicInfo.title,
            filePath = comicInfo.filePath,
            fileUri = comicInfo.fileUri,
            fileFormat = comicInfo.fileFormat,
            fileSize = comicInfo.fileSize,
            pageCount = comicInfo.pageCount,
            currentPage = 0,
            coverImagePath = coverPath,
            readingStatus = ReadingStatus.UNREAD,
            dateAdded = comicInfo.dateAdded,
            dateModified = System.currentTimeMillis()
        )
    }
    
    /**
     * 检查是否为图片文件
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = getFileExtension(fileName).lowercase()
        return IMAGE_EXTENSIONS.contains(extension)
    }
    
    /**
     * 检查是否为封面图片
     */
    private fun isCoverImage(fileName: String): Boolean {
        val name = getFileExtension(fileName).lowercase()
        return name.contains("cover") || 
               name.contains("front") || 
               name.contains("title") || 
               name.contains("000") ||
               name.contains("001")
    }
    
    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(fileName: String): String {
        return FilenameUtils.getExtension(fileName)
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    companion object {
        // 支持的文件格式
        val SUPPORTED_FORMATS = listOf("zip", "cbz", "rar", "cbr")
        
        // 支持的图片格式
        val IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
        
        // 最大文件大小 (2GB)
        const val MAX_FILE_SIZE = 2L * 1024 * 1024 * 1024
    }
}

/**
 * 导入结果
 */
data class ImportResult(
    val status: ImportStatus,
    val progress: Int = 0,
    val mangaId: Long? = null,
    val manga: Manga? = null,
    val error: String? = null
)

/**
 * 批量导入结果
 */
data class BatchImportResult(
    val status: BatchImportStatus,
    val currentIndex: Int = 0,
    val total: Int = 0,
    val currentFile: String? = null,
    val currentItemResult: ImportResult? = null,
    val results: List<ImportResult> = emptyList()
)

/**
 * 导入状态
 */
enum class ImportStatus {
    PROCESSING,    // 处理中
    PARSING,       // 解析中
    EXTRACTING_COVER, // 提取封面
    SAVING_TO_DATABASE, // 保存到数据库
    COMPLETED,     // 完成
    FAILED         // 失败
}

/**
 * 批量导入状态
 */
enum class BatchImportStatus {
    PROCESSING,       // 处理中
    ITEM_COMPLETED,    // 单个项目完成
    COMPLETED,         // 全部完成
    FAILED             // 失败
}

/**
 * 验证结果
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)