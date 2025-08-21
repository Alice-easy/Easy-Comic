package com.easycomic.memory

import android.graphics.Bitmap
import android.util.LruCache
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

/**
 * 内存优化管理器
 * 
 * 功能：
 * 1. 图片缓存管理
 * 2. 对象池管理
 * 3. 内存监控和清理
 * 4. 弱引用缓存
 */
class MemoryOptimizationManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: MemoryOptimizationManager? = null
        
        fun getInstance(): MemoryOptimizationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MemoryOptimizationManager().also { INSTANCE = it }
            }
        }
        
        // 内存阈值配置
        private const val MAX_MEMORY_PERCENTAGE = 0.25f // 使用最大内存的25%
        private const val LOW_MEMORY_THRESHOLD = 0.15f // 低内存阈值15%
        private const val CRITICAL_MEMORY_THRESHOLD = 0.05f // 临界内存阈值5%
    }
    
    // 图片缓存
    private val imageCache: LruCache<String, Bitmap>
    
    // 弱引用缓存（用于临时对象）
    private val weakReferenceCache = ConcurrentHashMap<String, WeakReference<Any>>()
    
    // 对象池管理器
    private val objectPoolManager = ObjectPoolManager()
    
    // 内存监控
    private val memoryMonitor = MemoryMonitor()
    
    // 缓存访问互斥锁
    private val cacheMutex = Mutex()
    
    init {
        // 计算可用内存大小
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = (maxMemory * MAX_MEMORY_PERCENTAGE).toInt()
        
        // 初始化图片缓存
        imageCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
            
            override fun entryRemoved(
                evicted: Boolean,
                key: String,
                oldValue: Bitmap,
                newValue: Bitmap?
            ) {
                if (evicted && !oldValue.isRecycled) {
                    oldValue.recycle()
                }
            }
        }
    }
    
    /**
     * 获取图片缓存
     */
    suspend fun getBitmap(key: String): Bitmap? = cacheMutex.withLock {
        return imageCache.get(key)
    }
    
    /**
     * 缓存图片
     */
    suspend fun cacheBitmap(key: String, bitmap: Bitmap) = cacheMutex.withLock {
        if (!bitmap.isRecycled) {
            imageCache.put(key, bitmap)
        }
    }
    
    /**
     * 移除图片缓存
     */
    suspend fun removeBitmap(key: String) = cacheMutex.withLock {
        imageCache.remove(key)
    }
    
    /**
     * 获取弱引用缓存对象
     */
    fun <T> getWeakReference(key: String): T? {
        return weakReferenceCache[key]?.get() as? T
    }
    
    /**
     * 设置弱引用缓存
     */
    fun setWeakReference(key: String, value: Any) {
        weakReferenceCache[key] = WeakReference(value)
    }
    
    /**
     * 清理弱引用缓存
     */
    fun cleanWeakReferences() {
        val iterator = weakReferenceCache.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.value.get() == null) {
                iterator.remove()
            }
        }
    }
    
    /**
     * 获取对象池管理器
     */
    fun getObjectPoolManager(): ObjectPoolManager = objectPoolManager
    
    /**
     * 获取内存监控器
     */
    fun getMemoryMonitor(): MemoryMonitor = memoryMonitor
    
    /**
     * 执行内存清理
     */
    suspend fun performMemoryCleanup(level: MemoryCleanupLevel = MemoryCleanupLevel.NORMAL) {
        when (level) {
            MemoryCleanupLevel.LIGHT -> {
                // 轻度清理：清理弱引用
                cleanWeakReferences()
            }
            MemoryCleanupLevel.NORMAL -> {
                // 正常清理：清理弱引用 + 部分图片缓存
                cleanWeakReferences()
                cacheMutex.withLock {
                    imageCache.trimToSize(imageCache.maxSize() / 2)
                }
            }
            MemoryCleanupLevel.AGGRESSIVE -> {
                // 激进清理：清理所有缓存
                cleanWeakReferences()
                cacheMutex.withLock {
                    imageCache.evictAll()
                }
                objectPoolManager.clearAllPools()
                System.gc() // 建议垃圾回收
            }
        }
    }
    
    /**
     * 检查内存状态并执行相应清理
     */
    suspend fun checkMemoryAndCleanup() {
        val memoryInfo = memoryMonitor.getCurrentMemoryInfo()
        val availableMemoryRatio = memoryInfo.availableMemory.toFloat() / memoryInfo.totalMemory
        
        when {
            availableMemoryRatio < CRITICAL_MEMORY_THRESHOLD -> {
                performMemoryCleanup(MemoryCleanupLevel.AGGRESSIVE)
            }
            availableMemoryRatio < LOW_MEMORY_THRESHOLD -> {
                performMemoryCleanup(MemoryCleanupLevel.NORMAL)
            }
            else -> {
                performMemoryCleanup(MemoryCleanupLevel.LIGHT)
            }
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            imageCacheSize = imageCache.size(),
            imageCacheMaxSize = imageCache.maxSize(),
            imageCacheHitCount = imageCache.hitCount(),
            imageCacheMissCount = imageCache.missCount(),
            weakReferenceCacheSize = weakReferenceCache.size,
            objectPoolStats = objectPoolManager.getPoolStats()
        )
    }
}

/**
 * 内存清理级别
 */
enum class MemoryCleanupLevel {
    LIGHT,      // 轻度清理
    NORMAL,     // 正常清理
    AGGRESSIVE  // 激进清理
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val imageCacheSize: Int,
    val imageCacheMaxSize: Int,
    val imageCacheHitCount: Long,
    val imageCacheMissCount: Long,
    val weakReferenceCacheSize: Int,
    val objectPoolStats: Map<String, ObjectPoolStats>
)

/**
 * 内存信息
 */
data class MemoryInfo(
    val totalMemory: Long,
    val availableMemory: Long,
    val usedMemory: Long,
    val maxMemory: Long
)