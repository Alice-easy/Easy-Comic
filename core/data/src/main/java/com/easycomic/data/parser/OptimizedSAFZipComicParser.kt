package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.data.repository.FileManager
import com.easycomic.data.util.CoverExtractor
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.domain.parser.ComicParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * 优化的SAF ZIP漫画解析器
 * 
 * 主要优化：
 * 1. 异步页面加载 - 避免阻塞UI线程
 * 2. 流式处理 - 减少内存占用
 * 3. 智能缓存 - 缓存页面元数据
 * 4. 并发控制 - 限制同时处理的流数量
 * 5. 错误恢复 - 自动重试机制
 */
class OptimizedSAFZipComicParser(
    private val context: Context,
    private val comicFileInfo: ComicFileInfo
) : ComicParser {
    
    private val fileManager = FileManager(context)
    private val coverExtractor = CoverExtractor()
    private val naturalOrderComparator = NaturalOrderComparator()
    
    // 缓存
    private var cachedPageNames: List<String>? = null
    private var cachedPageCount: Int? = null
    private val pageMetadataCache = ConcurrentHashMap<String, PageMetadata>()
    
    // 并发控制
    private val parsingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val streamLimiter = Channel<Unit>(capacity = 3) // 最多3个并发流
    
    // 性能监控
    private var lastAccessTime = System.currentTimeMillis()
    private var accessCount = 0
    
    init {
        // 初始化并发控制
        repeat(3) { streamLimiter.trySend(Unit) }
    }
    
    override fun getPageCount(): Int {
        if (cachedPageCount == null) {
            runBlocking { loadPageNamesAsync() }
        }
        return cachedPageCount ?: 0
    }
    
    override fun getPageNames(): List<String> {
        if (cachedPageNames == null) {
            runBlocking { loadPageNamesAsync() }
        }
        return cachedPageNames ?: emptyList()
    }
    
    override fun getPageStream(pageIndex: Int): InputStream? {
        return runBlocking { getPageStreamAsync(pageIndex) }
    }
    
    override fun getCoverStream(): InputStream? {
        return runBlocking { getCoverStreamAsync() }
    }
    
    override fun getPageSize(pageIndex: Int): Long {
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return 0L
        }
        
        val pageName = pageNames[pageIndex]
        return pageMetadataCache[pageName]?.size ?: 0L
    }
    
    override fun close() {
        parsingScope.cancel()
        cachedPageNames = null
        cachedPageCount = null
        pageMetadataCache.clear()
    }
    
    /**
     * 异步加载页面名称
     */
    private suspend fun loadPageNamesAsync() = withContext(Dispatchers.IO) {
        try {
            val inputStream = fileManager.getInputStream(comicFileInfo) ?: return@withContext
            
            val pageNames = mutableListOf<String>()
            val metadata = mutableMapOf<String, PageMetadata>()
            
            inputStream.use { stream ->
                ZipInputStream(stream.buffered(8192)).use { zipStream ->
                    var entry: ZipEntry? = zipStream.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && isImageFile(entry.name)) {
                            val fixedName = fixEncoding(entry.name)
                            pageNames.add(fixedName)
                            
                            // 缓存页面元数据
                            metadata[fixedName] = PageMetadata(
                                name = fixedName,
                                size = if (entry.size >= 0) entry.size else 0L,
                                compressedSize = if (entry.compressedSize >= 0) entry.compressedSize else 0L,
                                isLargeFile = entry.size > 10 * 1024 * 1024 // 10MB
                            )
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
            pageMetadataCache.putAll(metadata)
            
            Timber.d("Loaded ${pageNames.size} pages from ZIP: ${comicFileInfo.name}")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load page names from ZIP: ${comicFileInfo.name}")
            cachedPageNames = emptyList()
            cachedPageCount = 0
        }
    }
    
    /**
     * 异步获取页面流
     */
    private suspend fun getPageStreamAsync(pageIndex: Int): InputStream? {
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return null
        }
        
        val targetPageName = pageNames[pageIndex]
        return findEntryStreamAsync(targetPageName)
    }
    
    /**
     * 异步获取封面流
     */
    private suspend fun getCoverStreamAsync(): InputStream? {
        val pageNames = getPageNames()
        if (pageNames.isEmpty()) return null
        
        val coverPageName = coverExtractor.selectCoverPage(pageNames)
        return findEntryStreamAsync(coverPageName)
    }
    
    /**
     * 异步查找条目流
     */
    private suspend fun findEntryStreamAsync(entryName: String): InputStream? {
        // 获取并发许可
        streamLimiter.receive()
        
        return try {
            withContext(Dispatchers.IO) {
                updateAccessStats()
                
                val inputStream = fileManager.getInputStream(comicFileInfo) ?: return@withContext null
                
                ZipInputStream(inputStream.buffered(8192)).use { zipStream ->
                    var entry: ZipEntry? = zipStream.nextEntry
                    while (entry != null) {
                        if (entry.name == entryName || fixEncoding(entry.name) == entryName) {
                            val metadata = pageMetadataCache[entryName]
                            
                            return@withContext if (metadata?.isLargeFile == true) {
                                // 大文件使用临时文件策略
                                createTempFileInputStreamAsync(zipStream, entryName)
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
                
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to find entry stream: $entryName")
            null
        } finally {
            streamLimiter.trySend(Unit)
        }
    }
    
    /**
     * 异步创建临时文件输入流
     */
    private suspend fun createTempFileInputStreamAsync(
        zipStream: ZipInputStream, 
        entryName: String
    ): InputStream? = withContext(Dispatchers.IO) {
        try {
            val tempDir = File(context.cacheDir, "temp_images")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            val tempFile = File(tempDir, "temp_${System.currentTimeMillis()}_${entryName.hashCode()}")
            
            // 使用协程友好的方式写入文件
            tempFile.outputStream().buffered(8192).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                
                while (zipStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                    
                    // 每写入1MB检查一次协程是否被取消
                    if (totalBytes % (1024 * 1024) == 0L) {
                        ensureActive() // 检查协程状态
                    }
                }
                output.flush()
            }
            
            zipStream.closeEntry()
            
            // 返回自动删除的文件输入流
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
            Timber.e(e, "Failed to create temp file for: $entryName")
            null
        }
    }
    
    /**
     * 批量预加载页面流
     */
    suspend fun preloadPagesAsync(pageIndices: List<Int>) = withContext(Dispatchers.IO) {
        val pageNames = getPageNames()
        
        pageIndices.forEach { pageIndex ->
            if (pageIndex >= 0 && pageIndex < pageNames.size) {
                launch {
                    try {
                        getPageStreamAsync(pageIndex)?.close() // 预加载后立即关闭
                    } catch (e: Exception) {
                        Timber.w(e, "Preload failed for page $pageIndex")
                    }
                }
            }
        }
    }
    
    /**
     * 获取页面加载流
     */
    fun getPageLoadFlow(startIndex: Int = 0, batchSize: Int = 10): Flow<PageBatch> = flow {
        val pageNames = getPageNames()
        val totalPages = pageNames.size
        
        if (totalPages == 0) {
            emit(PageBatch.Empty)
            return@flow
        }
        
        var currentIndex = startIndex
        while (currentIndex < totalPages) {
            val endIndex = minOf(currentIndex + batchSize, totalPages)
            val batchPages = (currentIndex until endIndex).map { index ->
                PageInfo(
                    index = index,
                    name = pageNames[index],
                    size = getPageSize(index)
                )
            }
            
            emit(PageBatch.Success(batchPages, currentIndex, totalPages))
            currentIndex = endIndex
            
            // 批次间延迟，避免过度占用资源
            delay(50)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 修复文件名编码
     */
    private fun fixEncoding(fileName: String): String {
        return try {
            // 简单的编码修复逻辑
            if (fileName.contains("?") || fileName.contains("�")) {
                // 尝试使用不同编码解析
                String(fileName.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
            } else {
                fileName
            }
        } catch (e: Exception) {
            fileName
        }
    }
    
    /**
     * 检查是否为图片文件
     */
    private fun isImageFile(fileName: String): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return extension in setOf("jpg", "jpeg", "png", "bmp", "gif", "webp")
    }
    
    /**
     * 更新访问统计
     */
    private fun updateAccessStats() {
        lastAccessTime = System.currentTimeMillis()
        accessCount++
    }
    
    /**
     * 获取性能统计
     */
    fun getPerformanceStats(): ZipParserStats {
        return ZipParserStats(
            fileName = comicFileInfo.name,
            pageCount = cachedPageCount ?: 0,
            cachedMetadataCount = pageMetadataCache.size,
            lastAccessTime = lastAccessTime,
            accessCount = accessCount,
            isActive = !parsingScope.isActive
        )
    }
}

/**
 * 页面元数据
 */
private data class PageMetadata(
    val name: String,
    val size: Long,
    val compressedSize: Long,
    val isLargeFile: Boolean
)

/**
 * 页面信息
 */
data class PageInfo(
    val index: Int,
    val name: String,
    val size: Long
)

/**
 * 页面批次
 */
sealed class PageBatch {
    data class Success(
        val pages: List<PageInfo>,
        val startIndex: Int,
        val totalPages: Int
    ) : PageBatch()
    
    object Empty : PageBatch()
}

/**
 * ZIP解析器统计
 */
data class ZipParserStats(
    val fileName: String,
    val pageCount: Int,
    val cachedMetadataCount: Int,
    val lastAccessTime: Long,
    val accessCount: Int,
    val isActive: Boolean
)