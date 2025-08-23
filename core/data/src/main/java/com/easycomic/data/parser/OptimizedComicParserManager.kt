package com.easycomic.data.parser

import android.content.Context
import com.easycomic.data.repository.ComicFileInfo
import com.easycomic.domain.parser.ComicParser
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

/**
 * 优化的漫画解析器管理器
 * 
 * 主要优化：
 * 1. 异步分页加载 - 避免一次性加载所有页面
 * 2. 智能预加载 - 预加载当前页面前后的页面
 * 3. 内存管理 - 自动释放不需要的页面缓存
 * 4. 并发控制 - 限制同时解析的文件数量
 * 5. 错误恢复 - 自动重试和降级处理
 */
class OptimizedComicParserManager(
    private val context: Context,
    private val maxConcurrentParsers: Int = 3,
    private val preloadRange: Int = 2,
    private val maxCacheSize: Int = 20
) {
    
    // 解析器缓存
    private val parserCache = ConcurrentHashMap<String, ComicParser>()
    
    // 页面缓存
    private val pageCache = ConcurrentHashMap<String, CachedPage>()
    
    // 预加载任务管理
    private val preloadJobs = ConcurrentHashMap<String, Job>()
    
    // 并发控制
    private val parsingScope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + 
        CoroutineName("ComicParserManager")
    )
    private val concurrencyLimiter = Channel<Unit>(maxConcurrentParsers)
    
    // 统计信息
    private val activeParserCount = AtomicInteger(0)
    private val cacheHitCount = AtomicInteger(0)
    private val cacheMissCount = AtomicInteger(0)
    
    init {
        // 初始化并发控制信号量
        repeat(maxConcurrentParsers) {
            concurrencyLimiter.trySend(Unit)
        }
        
        // 启动缓存清理任务
        startCacheCleanupTask()
    }
    
    /**
     * 异步获取漫画解析器
     */
    suspend fun getParserAsync(comicFileInfo: ComicFileInfo): ComicParser? {
        val cacheKey = generateCacheKey(comicFileInfo)
        
        // 检查缓存
        parserCache[cacheKey]?.let { parser ->
            cacheHitCount.incrementAndGet()
            return parser
        }
        
        cacheMissCount.incrementAndGet()
        
        // 获取并发许可
        concurrencyLimiter.receive()
        
        return try {
            activeParserCount.incrementAndGet()
            
            val parser = withContext(Dispatchers.IO) {
                createParser(comicFileInfo)
            }
            
            parser?.let {
                parserCache[cacheKey] = it
            }
            
            parser
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to create parser for: ${comicFileInfo.name}")
            null
        } finally {
            activeParserCount.decrementAndGet()
            concurrencyLimiter.trySend(Unit)
        }
    }
    
    /**
     * 分页加载页面信息
     */
    suspend fun loadPagesAsync(
        comicFileInfo: ComicFileInfo,
        pageSize: Int = 10,
        startIndex: Int = 0
    ): Flow<PageLoadResult> = flow {
        val parser = getParserAsync(comicFileInfo) ?: run {
            emit(PageLoadResult.Error("无法创建解析器"))
            return@flow
        }
        
        val totalPages = parser.getPageCount()
        val pageNames = parser.getPageNames()
        
        if (totalPages == 0) {
            emit(PageLoadResult.Empty)
            return@flow
        }
        
        // 分页加载
        var currentIndex = startIndex
        while (currentIndex < totalPages) {
            val endIndex = min(currentIndex + pageSize, totalPages)
            val pageRange = currentIndex until endIndex
            
            try {
                val pages = pageRange.map { index ->
                    PageInfo(
                        index = index,
                        name = pageNames.getOrNull(index) ?: "Page $index",
                        size = parser.getPageSize(index)
                    )
                }
                
                emit(PageLoadResult.Success(pages, currentIndex, totalPages))
                
                // 预加载当前批次的图片
                preloadPages(comicFileInfo, pageRange.toList())
                
                currentIndex = endIndex
                
            } catch (e: Exception) {
                Timber.e(e, "Failed to load pages $currentIndex-${endIndex-1}")
                emit(PageLoadResult.Error("加载页面失败: ${e.message}"))
                break
            }
        }
    }
    
    /**
     * 获取页面流（带缓存和预加载）
     */
    suspend fun getPageStreamAsync(
        comicFileInfo: ComicFileInfo,
        pageIndex: Int
    ): InputStream? {
        val cacheKey = generatePageCacheKey(comicFileInfo, pageIndex)
        
        // 检查缓存
        pageCache[cacheKey]?.let { cachedPage ->
            if (!cachedPage.isExpired()) {
                cacheHitCount.incrementAndGet()
                return cachedPage.inputStream
            } else {
                // 移除过期缓存
                pageCache.remove(cacheKey)
            }
        }
        
        cacheMissCount.incrementAndGet()
        
        val parser = getParserAsync(comicFileInfo) ?: return null
        
        return try {
            val inputStream = withContext(Dispatchers.IO) {
                parser.getPageStream(pageIndex)
            }
            
            inputStream?.let { stream ->
                // 缓存页面（小页面才缓存）
                val pageSize = parser.getPageSize(pageIndex)
                if (pageSize > 0 && pageSize < 5 * 1024 * 1024) { // 5MB以下才缓存
                    val cachedPage = CachedPage(
                        inputStream = stream,
                        size = pageSize,
                        timestamp = System.currentTimeMillis()
                    )
                    pageCache[cacheKey] = cachedPage
                }
                
                // 启动智能预加载
                startSmartPreload(comicFileInfo, pageIndex)
                
                stream
            }
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to get page stream: $pageIndex")
            null
        }
    }
    
    /**
     * 批量预加载页面
     */
    private fun preloadPages(comicFileInfo: ComicFileInfo, pageIndices: List<Int>) {
        val preloadKey = generateCacheKey(comicFileInfo)
        
        // 取消之前的预加载任务
        preloadJobs[preloadKey]?.cancel()
        
        // 启动新的预加载任务
        preloadJobs[preloadKey] = parsingScope.launch {
            pageIndices.forEach { pageIndex ->
                if (isActive) {
                    try {
                        val cacheKey = generatePageCacheKey(comicFileInfo, pageIndex)
                        if (!pageCache.containsKey(cacheKey)) {
                            getPageStreamAsync(comicFileInfo, pageIndex)
                        }
                        
                        // 预加载间隔，避免过度占用资源
                        delay(100)
                        
                    } catch (e: Exception) {
                        Timber.w(e, "Preload failed for page $pageIndex")
                    }
                }
            }
        }
    }
    
    /**
     * 智能预加载
     * 根据当前页面预加载前后几页
     */
    private fun startSmartPreload(comicFileInfo: ComicFileInfo, currentPage: Int) {
        val preloadKey = "${generateCacheKey(comicFileInfo)}_smart_$currentPage"
        
        // 取消之前的智能预加载
        preloadJobs[preloadKey]?.cancel()
        
        preloadJobs[preloadKey] = parsingScope.launch {
            val parser = getParserAsync(comicFileInfo) ?: return@launch
            val totalPages = parser.getPageCount()
            
            // 计算预加载范围
            val startPage = maxOf(0, currentPage - preloadRange)
            val endPage = minOf(totalPages - 1, currentPage + preloadRange)
            
            // 优先加载后续页面（用户更可能往后翻）
            val preloadOrder = mutableListOf<Int>()
            
            // 添加后续页面
            for (i in currentPage + 1..endPage) {
                preloadOrder.add(i)
            }
            
            // 添加前面页面
            for (i in currentPage - 1 downTo startPage) {
                preloadOrder.add(i)
            }
            
            // 执行预加载
            preloadOrder.forEach { pageIndex ->
                if (isActive) {
                    try {
                        val cacheKey = generatePageCacheKey(comicFileInfo, pageIndex)
                        if (!pageCache.containsKey(cacheKey)) {
                            getPageStreamAsync(comicFileInfo, pageIndex)
                        }
                        
                        delay(200) // 智能预加载间隔更长
                        
                    } catch (e: Exception) {
                        Timber.w(e, "Smart preload failed for page $pageIndex")
                    }
                }
            }
        }
    }
    
    /**
     * 创建解析器
     */
    private suspend fun createParser(comicFileInfo: ComicFileInfo): ComicParser? {
        return try {
            when (comicFileInfo.name.substringAfterLast('.', "").lowercase()) {
                "zip", "cbz" -> OptimizedSAFZipComicParser(context, comicFileInfo)
                "rar", "cbr" -> OptimizedSAFRarComicParser(context, comicFileInfo)
                else -> {
                    Timber.w("Unsupported file format: ${comicFileInfo.name}")
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create parser for: ${comicFileInfo.name}")
            null
        }
    }
    
    /**
     * 启动缓存清理任务
     */
    private fun startCacheCleanupTask() {
        parsingScope.launch {
            while (isActive) {
                try {
                    cleanupCache()
                    delay(30000) // 30秒清理一次
                } catch (e: Exception) {
                    Timber.w(e, "Cache cleanup failed")
                }
            }
        }
    }
    
    /**
     * 清理过期缓存
     */
    private fun cleanupCache() {
        val currentTime = System.currentTimeMillis()
        val expiredKeys = mutableListOf<String>()
        
        // 清理过期页面缓存
        pageCache.forEach { (key, cachedPage) ->
            if (cachedPage.isExpired(currentTime)) {
                expiredKeys.add(key)
            }
        }
        
        expiredKeys.forEach { key ->
            pageCache.remove(key)?.inputStream?.close()
        }
        
        // 如果缓存过多，清理最旧的
        if (pageCache.size > maxCacheSize) {
            val sortedEntries = pageCache.entries.sortedBy { it.value.timestamp }
            val toRemove = sortedEntries.take(pageCache.size - maxCacheSize)
            
            toRemove.forEach { (key, cachedPage) ->
                pageCache.remove(key)
                cachedPage.inputStream.close()
            }
        }
        
        Timber.d("Cache cleanup completed. Removed ${expiredKeys.size} expired entries. Current cache size: ${pageCache.size}")
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(comicFileInfo: ComicFileInfo): String {
        return "${comicFileInfo.uri}_${comicFileInfo.size}_${comicFileInfo.lastModified}"
    }
    
    /**
     * 生成页面缓存键
     */
    private fun generatePageCacheKey(comicFileInfo: ComicFileInfo, pageIndex: Int): String {
        return "${generateCacheKey(comicFileInfo)}_page_$pageIndex"
    }
    
    /**
     * 获取性能统计
     */
    fun getPerformanceStats(): ParserPerformanceStats {
        return ParserPerformanceStats(
            activeParserCount = activeParserCount.get(),
            cachedParserCount = parserCache.size,
            cachedPageCount = pageCache.size,
            cacheHitCount = cacheHitCount.get(),
            cacheMissCount = cacheMissCount.get(),
            cacheHitRatio = if (cacheHitCount.get() + cacheMissCount.get() > 0) {
                cacheHitCount.get().toFloat() / (cacheHitCount.get() + cacheMissCount.get())
            } else 0f
        )
    }
    
    /**
     * 清理所有资源
     */
    fun cleanup() {
        // 取消所有预加载任务
        preloadJobs.values.forEach { it.cancel() }
        preloadJobs.clear()
        
        // 关闭所有解析器
        parserCache.values.forEach { parser ->
            try {
                parser.close()
            } catch (e: Exception) {
                Timber.w(e, "Failed to close parser")
            }
        }
        parserCache.clear()
        
        // 清理页面缓存
        pageCache.values.forEach { cachedPage ->
            try {
                cachedPage.inputStream.close()
            } catch (e: Exception) {
                Timber.w(e, "Failed to close cached page stream")
            }
        }
        pageCache.clear()
        
        // 取消协程作用域
        parsingScope.cancel()
    }
}

/**
 * 缓存的页面
 */
private data class CachedPage(
    val inputStream: InputStream,
    val size: Long,
    val timestamp: Long,
    val ttl: Long = 5 * 60 * 1000L // 5分钟TTL
) {
    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        return currentTime - timestamp > ttl
    }
}

/**
 * 页面信息
 */
data class PageInfo(
    val index: Int,
    val name: String,
    val size: Long
)

/**
 * 页面加载结果
 */
sealed class PageLoadResult {
    data class Success(
        val pages: List<PageInfo>,
        val startIndex: Int,
        val totalPages: Int
    ) : PageLoadResult()
    
    data class Error(val message: String) : PageLoadResult()
    object Empty : PageLoadResult()
}

/**
 * 解析器性能统计
 */
data class ParserPerformanceStats(
    val activeParserCount: Int,
    val cachedParserCount: Int,
    val cachedPageCount: Int,
    val cacheHitCount: Int,
    val cacheMissCount: Int,
    val cacheHitRatio: Float
)