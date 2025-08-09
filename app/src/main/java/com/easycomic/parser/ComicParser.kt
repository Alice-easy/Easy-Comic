package com.easycomic.parser

import com.easycomic.model.Comic
import com.easycomic.model.ComicPage
import com.easycomic.model.ParseResult
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/**
 * 简化的漫画文件解析器
 */
class ComicParser {
    
    /**
     * 解析漫画文件
     */
    suspend fun parseComicFile(filePath: String): Flow<ParseResult> = flow {
        Timber.d("开始解析文件: $filePath")
        
        try {
            emit(ParseResult.Loading(0.1f))
            
            val file = File(filePath)
            if (!file.exists()) {
                emit(ParseResult.Error("文件不存在: $filePath"))
                return@flow
            }
            
            emit(ParseResult.Loading(0.3f))
            
            val extension = filePath.substringAfterLast('.', "").lowercase()
            val (comic, pages) = when {
                extension == "zip" || extension == "cbz" -> parseZipFile(file)
                extension == "rar" || extension == "cbr" -> parseRarFile(file)
                else -> parseImageFile(file)
            }
            
            emit(ParseResult.Loading(1.0f))
            emit(ParseResult.Success(comic, pages))
            
        } catch (e: Exception) {
            Timber.e(e, "解析文件失败: $filePath")
            emit(ParseResult.Error("解析文件失败: ${e.message}", e))
        }
    }
    
    /**
     * 解析ZIP/CBZ文件
     */
    private suspend fun parseZipFile(file: File): Pair<Comic, List<ComicPage>> = withContext(Dispatchers.IO) {
        Timber.d("解析ZIP文件: ${file.name}")
        
        val imagePages = mutableListOf<ComicPage>()
        var pageCount = 0
        var coverImage: ByteArray? = null
        
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries.toList()
                .filter { !it.isDirectory }
                .filter { isImageFile(it.name) }
                .sortedWith(compareBy { naturalOrderKey(it.name) })
            
            pageCount = entries.size
            
            entries.forEachIndexed { index, entry ->
                val progress = 0.3f + (index.toFloat() / entries.size) * 0.6f
                
                zipFile.getInputStream(entry).use { inputStream ->
                    val imageData = inputStream.readBytes()
                    if (imageData.isNotEmpty()) {
                        val page = ComicPage(
                            index = index,
                            imageData = imageData,
                            imageFormat = getImageFormat(entry.name)
                        )
                        imagePages.add(page)
                        
                        // 第一张图片作为封面
                        if (index == 0) {
                            coverImage = imageData
                        }
                    }
                }
            }
        }
        
        val comic = Comic(
            title = file.nameWithoutExtension,
            filePath = file.absolutePath,
            fileUri = null,
            pageCount = pageCount,
            coverImage = coverImage
        )
        
        Pair(comic, imagePages)
    }
    
    /**
     * 解析RAR/CBR文件
     */
    private suspend fun parseRarFile(file: File): Pair<Comic, List<ComicPage>> = withContext(Dispatchers.IO) {
        Timber.d("解析RAR文件: ${file.name}")
        
        val imagePages = mutableListOf<ComicPage>()
        var pageCount = 0
        var coverImage: ByteArray? = null
        
        Archive(file).use { rarFile ->
            val fileHeaders = mutableListOf<FileHeader>()
            try {
                rarFile.fileHeaders.forEach { header ->
                    if (header.isDirectory.not() && isImageFile(header.fileName)) {
                        fileHeaders.add(header)
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "读取RAR文件头时出错")
            }
            
            // 按自然顺序排序
            fileHeaders.sortWith(compareBy { naturalOrderKey(it.fileName) })
            pageCount = fileHeaders.size
            
            fileHeaders.forEachIndexed { index, header ->
                try {
                    val progress = 0.3f + (index.toFloat() / fileHeaders.size) * 0.6f
                    
                    rarFile.getInputStream(header).use { inputStream ->
                        val imageData = inputStream.readBytes()
                        if (imageData.isNotEmpty()) {
                            val page = ComicPage(
                                index = index,
                                imageData = imageData,
                                imageFormat = getImageFormat(header.fileName)
                            )
                            imagePages.add(page)
                            
                            // 第一张图片作为封面
                            if (index == 0) {
                                coverImage = imageData
                            }
                        }
                    }
                } catch (e: Exception) {
                    Timber.w(e, "读取RAR文件页面失败: ${header.fileName}")
                }
            }
        }
        
        val comic = Comic(
            title = file.nameWithoutExtension,
            filePath = file.absolutePath,
            fileUri = null,
            pageCount = pageCount,
            coverImage = coverImage
        )
        
        Pair(comic, imagePages)
    }
    
    /**
     * 解析单个图片文件
     */
    private suspend fun parseImageFile(file: File): Pair<Comic, List<ComicPage>> = withContext(Dispatchers.IO) {
        Timber.d("解析图片文件: ${file.name}")
        
        val imageData = file.readBytes()
        val page = ComicPage(
            index = 0,
            imageData = imageData,
            imageFormat = getImageFormat(file.name)
        )
        
        val comic = Comic(
            title = file.nameWithoutExtension,
            filePath = file.absolutePath,
            fileUri = null,
            pageCount = 1,
            coverImage = imageData
        )
        
        Pair(comic, listOf(page))
    }
    
    /**
     * 检查是否为图片文件
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "webp", "gif", "bmp")
    }
    
    /**
     * 获取图片格式
     */
    private fun getImageFormat(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase()
    }
    
    /**
     * 自然排序键生成器
     * 将文件名转换为自然排序的键
     */
    private fun naturalOrderKey(fileName: String): String {
        // 提取文件名中的数字部分进行自然排序
        val name = fileName.substringAfterLast('/').substringAfterLast('\\')
        return name.replace(Regex("\\d+")) { matchResult ->
            matchResult.value.padStart(10, '0')
        }
    }
}