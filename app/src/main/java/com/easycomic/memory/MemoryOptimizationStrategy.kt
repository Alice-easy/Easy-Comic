package com.easycomic.memory

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * 内存优化策略集成管理器
 * 
 * 功能：
 * 1. 统一管理所有内存优化组件
 * 2. 提供简单易用的API接口
 * 3. 自动化内存监控和优化
 * 4. 性能指标收集和报告
 */
class MemoryOptimizationStrategy private constructor(
    private val context: Context
) {
    
    companion object {
        @Volatile
        private var INSTANCE: MemoryOptimizationStrategy? = null
        
        fun initialize(context: Context): MemoryOptimizationStrategy {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MemoryOptimizationStrategy(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
        
        fun getInstance(): MemoryOptimizationStrategy {
            return INSTANCE ?: throw IllegalStateException("MemoryOptimizationStrategy not initialized")
        }
    }
    
    // 核心组件
    private val memoryOptimizationManager = MemoryOptimizationManager.getInstance()
    private val objectPoolManager = memoryOptimizationManager.getObjectPoolManager()
    private val memoryMonitor = MemoryMonitor(context)
    private val imageCacheOptimizer = ImageCacheOptimizer(
        cacheDir = File(context.cacheDir, "optimized_images")
    )
    
    // 优化策略配置
    private val optimizationConfig = OptimizationConfig()
    private val strategyScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // 自动优化任务
    private var autoOptimizationJob: Job? = null
    private var isAutoOptimizationEnabled = false
    
    /**
     * 启动内存优化策略
     */
    fun start() {
        // 启动内存监控
        memoryMonitor.startMonitoring()
        
        // 启动自动优化
        if (optimizationConfig.enableAutoOptimization) {
            startAutoOptimization()
        }
    }
    
    /**
     * 停止内存优化策略
     */
    fun stop() {
        memoryMonitor.stopMonitoring()
        stopAutoOptimization()
    }
    
    /**
     * 获取内存状态流
     */
    fun getMemoryState(): StateFlow<MemoryState> = memoryMonitor.memoryState
    
    /**
     * 获取内存信息流
     */
    fun getMemoryInfo(): StateFlow<MemoryInfo> = memoryMonitor.memoryInfo
    
    /**
     * 执行手动内存优化
     */
    suspend fun performOptimization(level: OptimizationLevel = OptimizationLevel.SMART) {
        when (level) {
            OptimizationLevel.LIGHT -> {
                memoryOptimizationManager.performMemoryCleanup(MemoryCleanupLevel.LIGHT)
                imageCacheOptimizer.cleanCache(CacheCleanLevel.LIGHT)
            }
            OptimizationLevel.NORMAL -> {
                memoryOptimizationManager.performMemoryCleanup(MemoryCleanupLevel.NORMAL)
                imageCacheOptimizer.cleanCache(CacheCleanLevel.NORMAL)
            }
            OptimizationLevel.AGGRESSIVE -> {
                memoryOptimizationManager.performMemoryCleanup(MemoryCleanupLevel.AGGRESSIVE)
                imageCacheOptimizer.cleanCache(CacheCleanLevel.AGGRESSIVE)
            }
            OptimizationLevel.SMART -> {
                // 智能优化：根据当前内存状态选择合适的优化级别
                val memoryState = memoryMonitor.memoryState.value
                val optimizationLevel = when (memoryState) {
                    MemoryState.CRITICAL -> OptimizationLevel.AGGRESSIVE
                    MemoryState.LOW -> OptimizationLevel.NORMAL
                    MemoryState.NORMAL -> OptimizationLevel.LIGHT
                }
                performOptimization(optimizationLevel)
            }
        }
    }
    
    /**
     * 获取优化后的图片
     */
    suspend fun getOptimizedImage(
        imagePath: String,
        targetWidth: Int = 0,
        targetHeight: Int = 0,
        quality: ImageQuality = ImageQuality.MEDIUM
    ) = imageCacheOptimizer.getOptimizedImage(imagePath, targetWidth, targetHeight, quality)
    
    /**
     * 预加载图片
     */
    fun preloadImages(imagePaths: List<String>, priority: LoadPriority = LoadPriority.LOW) {
        imageCacheOptimizer.preloadImages(imagePaths, priority)
    }
    
    /**
     * 获取对象池
     */
    fun <T> getObjectPool(
        poolName: String,
        factory: () -> T,
        reset: (T) -> Unit = {},
        maxSize: Int = 10
    ) = objectPoolManager.getPool(poolName, factory, reset, maxSize)
    
    /**
     * 获取常用对象池
     */
    fun getByteArrayPool(bufferSize: Int = 8192) = 
        CommonObjectPools.getByteArrayPool(objectPoolManager, bufferSize)
    
    fun getStringBuilderPool() = 
        CommonObjectPools.getStringBuilderPool(objectPoolManager)
    
    fun <T> getArrayListPool(poolName: String = "ArrayList") = 
        CommonObjectPools.getArrayListPool<T>(objectPoolManager, poolName)
    
    /**
     * 获取内存优化报告
     */
    suspend fun getOptimizationReport(): MemoryOptimizationReport {
        val memoryStats = memoryMonitor.getDetailedMemoryStats()
        val cacheStats = memoryOptimizationManager.getCacheStats()
        val imageStats = imageCacheOptimizer.getCacheStatistics()
        val suggestions = memoryMonitor.getMemoryOptimizationSuggestions()
        
        return MemoryOptimizationReport(
            memoryStats = memoryStats,
            cacheStats = cacheStats,
            imageStats = imageStats,
            suggestions = suggestions,
            timestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * 更新优化配置
     */
    fun updateConfig(config: OptimizationConfig) {
        // 更新配置并重启相关服务
        if (config.enableAutoOptimization != optimizationConfig.enableAutoOptimization) {
            if (config.enableAutoOptimization) {
                startAutoOptimization()
            } else {
                stopAutoOptimization()
            }
        }
    }
    
    /**
     * 启动自动优化
     */
    private fun startAutoOptimization() {
        if (isAutoOptimizationEnabled) return
        
        isAutoOptimizationEnabled = true
        autoOptimizationJob = strategyScope.launch {
            while (isActive && isAutoOptimizationEnabled) {
                try {
                    // 检查内存状态并执行相应优化
                    memoryOptimizationManager.checkMemoryAndCleanup()
                    
                    // 等待下次检查
                    delay(optimizationConfig.autoOptimizationInterval)
                } catch (e: Exception) {
                    // 自动优化异常处理
                    e.printStackTrace()
                }
            }
        }
    }
    
    /**
     * 停止自动优化
     */
    private fun stopAutoOptimization() {
        isAutoOptimizationEnabled = false
        autoOptimizationJob?.cancel()
        autoOptimizationJob = null
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        stop()
        imageCacheOptimizer.cleanup()
        memoryMonitor.cleanup()
        strategyScope.cancel()
    }
}

/**
 * 优化级别
 */
enum class OptimizationLevel {
    LIGHT,      // 轻度优化
    NORMAL,     // 正常优化
    AGGRESSIVE, // 激进优化
    SMART       // 智能优化（根据当前状态自动选择）
}

/**
 * 优化配置
 */
data class OptimizationConfig(
    val enableAutoOptimization: Boolean = true,
    val autoOptimizationInterval: Long = 30000L, // 30秒
    val enableImageOptimization: Boolean = true,
    val enableObjectPooling: Boolean = true,
    val maxMemoryUsageRatio: Float = 0.8f,
    val lowMemoryThreshold: Float = 0.15f
)

/**
 * 内存优化报告
 */
data class MemoryOptimizationReport(
    val memoryStats: DetailedMemoryStats,
    val cacheStats: CacheStats,
    val imageStats: ImageCacheStatistics,
    val suggestions: List<MemoryOptimizationSuggestion>,
    val timestamp: Long
) {
    /**
     * 生成优化报告摘要
     */
    fun generateSummary(): String {
        val sb = StringBuilder()
        sb.appendLine("=== 内存优化报告 ===")
        sb.appendLine("生成时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(timestamp)}")
        sb.appendLine()
        
        // 内存使用情况
        sb.appendLine("内存使用情况:")
        sb.appendLine("  堆内存使用率: ${String.format("%.1f%%", memoryStats.heapUsageRatio * 100)}")
        sb.appendLine("  已分配内存: ${formatBytes(memoryStats.heapAllocated)}")
        sb.appendLine("  最大堆内存: ${formatBytes(memoryStats.maxHeap)}")
        sb.appendLine()
        
        // 缓存统计
        sb.appendLine("缓存统计:")
        sb.appendLine("  图片缓存命中率: ${String.format("%.1f%%", 
            cacheStats.imageCacheHitCount.toFloat() / 
            (cacheStats.imageCacheHitCount + cacheStats.imageCacheMissCount) * 100)}")
        sb.appendLine("  内存缓存使用: ${formatBytes(imageStats.memoryCacheSize.toLong())}")
        sb.appendLine("  磁盘缓存使用: ${formatBytes(imageStats.diskCacheSize)}")
        sb.appendLine()
        
        // 优化建议
        if (suggestions.isNotEmpty()) {
            sb.appendLine("优化建议:")
            suggestions.forEach { suggestion ->
                sb.appendLine("  [${suggestion.priority}] ${suggestion.description}")
            }
        }
        
        return sb.toString()
    }
    
    private fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.1f %s", size, units[unitIndex])
    }
}

/**
 * 内存优化策略扩展函数
 */

/**
 * 使用对象池执行操作
 */
suspend inline fun <T, R> MemoryOptimizationStrategy.useObjectPool(
    poolName: String,
    noinline factory: () -> T,
    noinline reset: (T) -> Unit = {},
    maxSize: Int = 10,
    block: (T) -> R
): R {
    val pool = getObjectPool(poolName, factory, reset, maxSize)
    return pool.use(block)
}

/**
 * 批量预加载图片
 */
fun MemoryOptimizationStrategy.preloadImagesInBatches(
    imagePaths: List<String>,
    batchSize: Int = 10,
    priority: LoadPriority = LoadPriority.LOW
) {
    imagePaths.chunked(batchSize).forEach { batch ->
        preloadImages(batch, priority)
    }
}