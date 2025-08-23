package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.data.repository.FileManager
import com.easycomic.data.util.CoverExtractor
import com.easycomic.data.util.EncodingUtils
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.domain.parser.ComicParser
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 支持SAF的ZIP漫画解析器
 * 通过Android Storage Access Framework访问ZIP文件
 */
class SAFZipComicParser(
    private val context: Context,
    private val comicFileInfo: ComicFileInfo
) : ComicParser {
    
    private val fileManager = FileManager(context)
    private val coverExtractor = CoverExtractor()
    private val naturalOrderComparator = NaturalOrderComparator()
    
    private var cachedPageNames: List<String>? = null
    private var cachedPageCount: Int? = null
    
    override fun getPageCount(): Int {
        if (cachedPageCount == null) {
            loadPageNames()
        }
        return cachedPageCount ?: 0
    }
    
    override fun getPageNames(): List<String> {
        if (cachedPageNames == null) {
            loadPageNames()
        }
        return cachedPageNames ?: emptyList()
    }
    
    override fun getPageStream(pageIndex: Int): InputStream? {
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return null
        }
        
        val targetPageName = pageNames[pageIndex]
        return findEntryStream(targetPageName)
    }
    
    override fun getCoverStream(): InputStream? {
        val pageNames = getPageNames()
        if (pageNames.isEmpty()) return null
        
        // 使用封面提取器选择最佳封面
        val coverPageName = CoverExtractor.selectCoverPage(pageNames)
        return findEntryStream(coverPageName)
    }
    
    override fun getPageSize(pageIndex: Int): Long {
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return 0L
        }
        
        val targetPageName = pageNames[pageIndex]
        return findEntrySize(targetPageName)
    }
    
    override fun close() {
        // SAF流在使用后自动关闭，这里不需要额外操作
        cachedPageNames = null
        cachedPageCount = null
    }
    
    /**
     * 加载页面名称列表
     */
    private fun loadPageNames() {
        try {
            val inputStream = runBlocking { fileManager.getInputStream(comicFileInfo) }
                ?: return
            
            val pageNames = mutableListOf<String>()
            
            inputStream.use { stream ->
                ZipInputStream(stream).use { zipStream ->
                    var entry: ZipEntry? = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && isImageFile(entry.name)) {
                            // 修复文件名编码问题
                            val fixedName = EncodingUtils.fixEncoding(entry.name)
                            pageNames.add(fixedName)
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
            
            // 按自然顺序排序
            pageNames.sortWith(naturalOrderComparator)
            
            cachedPageNames = pageNames
            cachedPageCount = pageNames.size
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load page names from SAF ZIP: ${comicFileInfo.name}")
            cachedPageNames = emptyList()
            cachedPageCount = 0
        }
    }
    
    /**
     * 查找指定条目的输入流
     * 优化：避免将整个条目读入内存，使用流式处理
     */
    private fun findEntryStream(entryName: String): InputStream? {
        try {
            val inputStream = runBlocking { fileManager.getInputStream(comicFileInfo) }
                ?: return null
            
            ZipInputStream(inputStream.buffered(8192)).use { zipStream ->
                var entry: ZipEntry? = zipStream.nextEntry
                while (entry != null) {
                    if (entry.name == entryName) {
                        // 对于大图片文件，使用临时文件避免内存问题
                        val entrySize = entry.size
                        
                        return if (entrySize > 0 && entrySize > 10 * 1024 * 1024) { // 10MB以上使用临时文件
                            createTempFileInputStream(zipStream, entryName)
                        } else {
                            // 小文件直接读取到内存
                            val bytes = zipStream.readBytes()
                            zipStream.closeEntry()
                            bytes.inputStream()
                        }
                    }
                    zipStream.closeEntry()
                    entry = zipStream.nextEntry
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find entry stream: $entryName in ${comicFileInfo.name}")
        }
        
        return null
    }
    
    /**
     * 为大文件创建临时文件输入流
     */
    private fun createTempFileInputStream(zipStream: ZipInputStream, entryName: String): InputStream? {
        return try {
            val tempDir = File(context.cacheDir, "temp_images")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            val tempFile = File(tempDir, "temp_${System.currentTimeMillis()}_${entryName.hashCode()}")
            
            tempFile.outputStream().buffered(8192).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (zipStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }
                output.flush()
            }
            
            zipStream.closeEntry()
            
            // 返回会自动删除临时文件的输入流
            object : java.io.FileInputStream(tempFile) {
                override fun close() {
                    super.close()
                    try {
                        tempFile.delete()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to delete temp file: ${tempFile.absolutePath}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create temp file for large entry: $entryName")
            null
        }
    }
    
    /**
     * 查找指定条目的大小
     */
    private fun findEntrySize(entryName: String): Long {
        try {
            val inputStream = runBlocking { fileManager.getInputStream(comicFileInfo) }
                ?: return 0L
            
            inputStream.use { stream ->
                ZipInputStream(stream).use { zipStream ->
                    var entry: ZipEntry? = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name == entryName) {
                            val size = entry.size
                            zipStream.closeEntry()
                            return if (size >= 0) size else 0L
                        }
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find entry size: $entryName in ${comicFileInfo.name}")
        }
        
        return 0L
    }
    
    /**
     * 检查文件是否为图片
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "bmp", "gif", "webp")
    }
}
