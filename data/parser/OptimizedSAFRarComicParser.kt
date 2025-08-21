package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.data.repository.FileManager
import com.easycomic.data.util.CoverExtractor
import com.easycomic.data.util.NaturalOrderComparator
import com.easycomic.domain.parser.ComicParser
import com.github.junrar.Archive
import com.github.junrar.extract.ExtractArchive
import com.github.junrar.rarfile.FileHeader
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * 优化的SAF RAR漫画解析器
 * 
 * 主要优化：
 * 1. 异步初始化 - 避免阻塞主线程
 * 2. 流式复制 - 减少内存占用
 * 3. 智能缓存 - 缓存提取的文件
 * 4. 并发控制 - 限制同时处理的提取操作
 * 5. 资源管理 - 自动清理临时文件
 */
class OptimizedSAFRarComicParser(
    private val context: Context,
    private val comicFileInfo: ComicFileInfo
) : ComicParser {
    
    private val fileManager = FileManager(context)
    private val coverExtractor = CoverExtractor()
    private val naturalOrderComparator = NaturalOrderComparator()
    
    // 核心组件
    private var tempFile: File? = null
    private var archive: Archive? = null
    private var cachedPageNames: List<String>? = null
    private var cachedPageCount: Int? = null
    
    // 缓存和并发控制
    private val extractedFileCache = ConcurrentHashMap<String, ByteArray>()
    private val pageMetadataCache = ConcurrentHashMap<String, RarPageMetadata>()
    private val parsingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val extractionLimiter = Channel<Unit>(capacity = 2) // 最多2个并发提取
    
    // 状态管理
    @Volatile
    private var isInitialized = false
    @Volatile
    private var initializationError: Exception? = null
    
    // 性能监控
    private var lastAccessTime = System.currentTimeMillis()
    private var extractionCount = 0
    
    init {
        // 初始化并发控制
        repeat(2) { extractionLimiter.trySend(Unit) }
        
        // 异步初始化
        parsingScope.launch {
            try {
                initializeArchiveAsync()
                isInitialized = true
            } catch (e: Exception) {
                initializationError = e
                Timber.e(e, "Failed to initialize RAR parser: ${comicFileInfo.name}")
            }
        }
    }
    
    override fun getPageCount(): Int {
        waitForInitialization()
        if (cachedPageCount == null) {
            runBlocking { loadPageNamesAsync() }
        }
        return cachedPageCount ?: 0
    }
    
    override fun getPageNames(): List<String> {
        waitForInitialization()
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
        return pageMetadataCache[pageName]?.unpackedSize ?: 0L
    }
    
    override fun close() {
        cleanup()
    }
    
    /**
     * 异步初始化RAR档案
     */
    private suspend fun initializeArchiveAsync() = withContext(Dispatchers.IO) {
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
            
            // 流式复制文件
            val inputStream = fileManager.getInputStream(comicFileInfo)
                ?: throw Exception("Cannot open input stream for SAF file")
            
            inputStream.buffered(8192).use { input ->
                tempFile!!.outputStream().buffered(8192).use { output ->
                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0L
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        ensureActive() // 检查协程状态
                        
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        
                        // 定期检查进度
                        if (totalBytesRead % (1024 * 1024) == 0L) {
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
            
            Timber.d("RAR档案初始化成功: ${comicFileInfo.name}, 大小: ${tempFile!!.length() / 1024 / 1024}MB")
            
        } catch (e: Exception) {
            cleanup()
            throw e
        }
    }
    
    /**
     * 等待初始化完成
     */
    private fun waitForInitialization() {
        if (!isInitialized && initializationError == null) {
            runBlocking {
                // 等待初始化完成，最多等待30秒
                withTimeoutOrNull(30000) {
                    while (!isInitialized && initializationError == null) {
                        delay(100)
                    }
                }
            }
        }
        
        initializationError?.let { throw it }
    }
    
    /**
     * 异步加载页面名称
     */
    private suspend fun loadPageNamesAsync() = withContext(Dispatchers.IO) {
        val currentArchive = archive ?: return@withContext
        
        try {
            val pageNames = mutableListOf<String>()
            val metadata = mutableMapOf<String, RarPageMetadata>()
            
            for (fileHeader in currentArchive.fileHeaders) {
                if (!fileHeader.isDirectory && isImageFile(fileHeader.fileName)) {
                    pageNames.add(fileHeader.fileName)
                    
                    // 缓存页面元数据
                    metadata[fileHeader.fileName] = RarPageMetadata(
                        name = fileHeader.fileName,
                        unpackedSize = fileHeader.fullUnpackSize,
                        packedSize = fileHeader.fullPackSize,
                        isLargeFile = fileHeader.fullUnpackSize > 5 * 1024 * 1024 // 5MB
                    )
                }
            }
            
            // 按自然顺序排序
            pageNames.sortWith(naturalOrderComparator)
            
            cachedPageNames = pageNames
            cachedPageCount = pageNames.size
            pageMetadataCache.putAll(metadata)
            
            Timber.d("Loaded ${pageNames.size} pages from RAR: ${comicFileInfo.name}")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to load page names from RAR: ${comicFileInfo.name}")
            cachedPageNames = emptyList()
            cachedPageCount = 0
        }
    }
    
    /**
     * 异步获取页面流
     */
    private suspend fun getPageStreamAsync(pageIndex: Int): InputStream? {
        waitForInitialization()
        
        val pageNames = getPageNames()
        if (pageIndex < 0 || pageIndex >= pageNames.size) {
            return null
        }
        
        val targetPageName = pageNames[pageIndex]
        return extractFileToStreamAsync(targetPageName)
    }
    
    /**
     * 异步获取封面流
     */
    private suspend fun getCoverStreamAsync(): InputStream? {
        waitForInitialization()
        
        val pageNames = getPageNames()
        if (pageNames.isEmpty()) return null
        
        val coverPageName = coverExtractor.selectCoverPage(pageNames)
        return extractFileToStreamAsync(coverPageName)
    }
    
    /**
     * 异步提取文件到流
     */
    private suspend fun extractFileToStreamAsync(fileName: String): InputStream? {
        // 检查缓存
        extractedFileCache[fileName]?.let { cachedData ->
            return cachedData.inputStream()
        }
        
        // 获取并发许可
        extractionLimiter.receive()
        
        return try {
            withContext(Dispatchers.IO) {
                updateAccessStats()
                
                val currentArchive = archive ?: return@withContext null
                val fileHeader = currentArchive.fileHeaders.find { it.fileName == fileName }
                    ?: return@withContext null
                
                val outputStream = ByteArrayOutputStream()
                ExtractArchive.extractFile(currentArchive, fileHeader, outputStream)
                
                val extractedData = outputStream.toByteArray()
                
                // 缓存小文件
                val metadata = pageMetadataCache[fileName]
                if (metadata?.isLargeFile != true) {
                    extractedFileCache[fileName] = extractedData
                    
                    // 限制缓存大小
                    if (extractedFileCache.size > 10) {
                        cleanupCache()
                    }
                }
                
                extractedData.inputStream()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to extract file: $fileName")
            null
        } finally {
            extractionLimiter.trySend(Unit)
        }
    }
    
    /**
     * 批量预提取页面
     */
    suspend fun preExtractPagesAsync(pageIndices: List<Int>) = withContext(Dispatchers.IO) {
        val pageNames = getPageNames()
        
        pageIndices.forEach { pageIndex ->
            if (pageIndex >= 0 && pageIndex < pageNames.size) {
                launch {
                    try {
                        val pageName = pageNames[pageIndex]
                        if (!extractedFileCache.containsKey(pageName)) {
                            extractFileToStreamAsync(pageName)?.close()
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Pre-extraction failed for page $pageIndex")
                    }
                }
            }
        }
    }
    
    /**
     * 获取页面加载流
     */
    fun getPageLoadFlow(startIndex: Int = 0, batchSize: Int = 5): Flow<RarPageBatch> = flow {
        waitForInitialization()
        
        val pageNames = getPageNames()
        val totalPages = pageNames.size
        
        if (totalPages == 0) {
            emit(RarPageBatch.Empty)
            return@flow
        }
        
        var currentIndex = startIndex
        while (currentIndex < totalPages) {
            val endIndex = minOf(currentIndex + batchSize, totalPages)
            val batchPages = (currentIndex until endIndex).map { index ->
                RarPageInfo(
                    index = index,
                    name = pageNames[index],
                    unpackedSize = getPageSize(index),
                    isCached = extractedFileCache.containsKey(pageNames[index])
                )
            }
            
            emit(RarPageBatch.Success(batchPages, currentIndex, totalPages))
            currentIndex = endIndex
            
            // 批次间延迟
            delay(100)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 清理缓存
     */
    private fun cleanupCache() {
        if (extractedFileCache.size > 10) {
            // 移除最旧的缓存项
            val keysToRemove = extractedFileCache.keys.take(extractedFileCache.size - 8)
            keysToRemove.forEach { key ->
                extractedFileCache.remove(key)
            }
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
        extractionCount++
    }
    
    /**
     * 清理所有资源
     */
    private fun cleanup() {
        parsingScope.cancel()
        
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
        extractedFileCache.clear()
        pageMetadataCache.clear()
    }
    
    /**
     * 获取性能统计
     */
    fun getPerformanceStats(): RarParserStats {
        return RarParserStats(
            fileName = comicFileInfo.name,
            pageCount = cachedPageCount ?: 0,
            cachedFileCount = extractedFileCache.size,
            cachedMetadataCount = pageMetadataCache.size,
            lastAccessTime = lastAccessTime,
            extractionCount = extractionCount,
            isInitialized = isInitialized,
            tempFileSize = tempFile?.length() ?: 0L
        )
    }
}

/**
 * RAR页面元数据
 */
private data class RarPageMetadata(
    val name: String,
    val unpackedSize: Long,
    val packedSize: Long,
    val isLargeFile: Boolean
)

/**
 * RAR页面信息
 */
data class RarPageInfo(
    val index: Int,
    val name: String,
    val unpackedSize: Long,
    val isCached: Boolean
)

/**
 * RAR页面批次
 */
sealed class RarPageBatch {
    data class Success(
        val pages: List<RarPageInfo>,
        val startIndex: Int,
        val totalPages: Int
    ) : RarPageBatch()
    
    object Empty : RarPageBatch()
}

/**
 * RAR解析器统计
 */
data class RarParserStats(
    val fileName: String,
    val pageCount: Int,
    val cachedFileCount: Int,
    val cachedMetadataCount: Int,
    val lastAccessTime: Long,
    val extractionCount: Int,
    val isInitialized: Boolean,
    val tempFileSize: Long
)