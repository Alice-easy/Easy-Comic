package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.data.repository.FileManager
import com.easycomic.data.util.CoverExtractor
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.domain.parser.ComicParser
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

/**
 * 支持SAF的RAR漫画解析器
 * 通过Android Storage Access Framework访问RAR文件
 * 
 * 注意：由于JunRar库需要随机访问，SAF的RAR文件需要先复制到临时文件
 */
class SAFRarComicParser(
    private val context: Context,
    private val comicFileInfo: ComicFileInfo
) : ComicParser {
    
    private val fileManager = FileManager(context)
    private val coverExtractor = CoverExtractor()
    private val naturalOrderComparator = NaturalOrderComparator()
    
    private var tempFile: File? = null
    private var archive: Archive? = null
    private var cachedPageNames: List<String>? = null
    private var cachedPageCount: Int? = null
    
    init {
        initializeArchive()
    }
    
    /**
     * 初始化RAR档案
     * SAF文件需要先复制到临时文件，因为JunRar需要随机访问
     * 优化：使用流式复制避免大文件内存溢出
     */
    private fun initializeArchive() {
        try {
            // 检查可用空间
            val availableSpace = context.cacheDir.freeSpace
            val fileSize = comicFileInfo.size
            
            if (fileSize > 0 && availableSpace < fileSize * 2) {
                throw Exception("磁盘空间不足，需要 ${fileSize / 1024 / 1024}MB 可用空间")
            }
            
            // 创建临时文件
            val tempDir = File(context.cacheDir, "temp_comics")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            tempFile = File(tempDir, "temp_${System.currentTimeMillis()}.rar")
            
            // 使用缓冲流式复制，避免大文件内存问题
            val inputStream = runBlocking { fileManager.getInputStream(comicFileInfo) }
                ?: throw Exception("Cannot open input stream for SAF file")
            
            inputStream.buffered(8192).use { input ->
                tempFile!!.outputStream().buffered(8192).use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // 对于大文件，定期检查是否取消操作
                        if (totalBytesRead % (1024 * 1024) == 0L) { // 每1MB检查一次
                            // 这里可以添加取消逻辑或进度回调
                            Timber.d("复制进度: ${totalBytesRead / 1024 / 1024}MB")
                        }
                    }
                    
                    output.flush()
                }
            }
            
            // 验证文件完整性
            if (tempFile!!.length() == 0L) {
                throw Exception("临时文件创建失败，文件大小为0")
            }
            
            // 打开RAR档案
            archive = Archive(tempFile)
            
            Timber.d("SAF RAR档案初始化成功: ${comicFileInfo.name}, 大小: ${tempFile!!.length() / 1024 / 1024}MB")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize SAF RAR archive: ${comicFileInfo.name}")
            cleanup()
            throw e // 重新抛出异常，让调用者处理
        }
    }
    
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
        return extractFileToStream(targetPageName)
    }
    
    override fun getCoverStream(): InputStream? {
        val pageNames = getPageNames()
        if (pageNames.isEmpty()) return null
        
        // 使用封面提取器选择最佳封面
        val coverPageName = CoverExtractor.selectCoverPage(pageNames)
        return extractFileToStream(coverPageName)
    }
    
    override fun getPageSize(pageIndex: Int): Long {
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return 0L
        }
        
        val targetPageName = pageNames[pageIndex]
        return getFileSize(targetPageName)
    }
    
    override fun close() {
        cleanup()
    }
    
    /**
     * 清理资源
     */
    private fun cleanup() {
        try {
            archive?.close()
        } catch (e: Exception) {
            Timber.w(e, "Failed to close RAR archive")
        }
        
        tempFile?.let { file ->
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to delete temp file: ${file.absolutePath}")
            }
        }
        
        archive = null
        tempFile = null
        cachedPageNames = null
        cachedPageCount = null
    }
    
    /**
     * 加载页面名称列表
     */
    private fun loadPageNames() {
        val currentArchive = archive ?: return
        
        try {
            val pageNames = mutableListOf<String>()
            
            for (fileHeader in currentArchive.fileHeaders) {
                if (!fileHeader.isDirectory && isImageFile(fileHeader.fileName)) {
                    pageNames.add(fileHeader.fileName)
                }
            }
            
            // 按自然顺序排序
            pageNames.sortWith(naturalOrderComparator)
            
            cachedPageNames = pageNames
            cachedPageCount = pageNames.size
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load page names from SAF RAR: ${comicFileInfo.name}")
            cachedPageNames = emptyList()
            cachedPageCount = 0
        }
    }
    
    /**
     * 提取文件到输入流
     */
    private fun extractFileToStream(fileName: String): InputStream? {
        val currentArchive = archive ?: return null
        
        try {
            val fileHeader = currentArchive.fileHeaders.find { it.fileName == fileName }
                ?: return null
            
            val outputStream = ByteArrayOutputStream()
            currentArchive.extractFile(fileHeader, outputStream)
            
            return outputStream.toByteArray().inputStream()
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract file: $fileName from ${comicFileInfo.name}")
            return null
        }
    }
    
    /**
     * 获取文件大小
     */
    private fun getFileSize(fileName: String): Long {
        val currentArchive = archive ?: return 0L
        
        try {
            val fileHeader = currentArchive.fileHeaders.find { it.fileName == fileName }
                ?: return 0L
            
            return fileHeader.fullUnpackSize
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file size: $fileName from ${comicFileInfo.name}")
            return 0L
        }
    }
    
    /**
     * 检查文件是否为图片
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "bmp", "gif", "webp")
    }
}
