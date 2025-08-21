package com.easycomic.memory

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.min

/**
 * 图片缓存优化器
 * 
 * 功能：
 * 1. 智能图片压缩和缓存
 * 2. 多级缓存策略（内存+磁盘）
 * 3. 图片预加载和懒加载
 * 4. 缓存清理和优化
 */
class ImageCacheOptimizer(
    private val cacheDir: File,
    private val maxMemoryCacheSize: Int = 50 * 1024 * 1024, // 50MB
    private val maxDiskCacheSize: Long = 200 * 1024 * 1024L // 200MB
) {
    
    private val memoryOptimizationManager = MemoryOptimizationManager.getInstance()
    private val diskCacheDir = File(cacheDir, "image_cache")
    private val cacheMutex = Mutex()
    private val compressionScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // 图片压缩配置
    private val compressionConfig = ImageCompressionConfig()
    
    init {
        // 确保缓存目录存在
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
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
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = generateCacheKey(imagePath, targetWidth, targetHeight, quality)
        
        // 1. 尝试从内存缓存获取
        memoryOptimizationManager.getBitmap(cacheKey)?.let { return@withContext it }
        
        // 2. 尝试从磁盘缓存获取
        loadFromDiskCache(cacheKey)?.let { bitmap ->
            memoryOptimizationManager.cacheBitmap(cacheKey, bitmap)
            return@withContext bitmap
        }
        
        // 3. 加载并优化原图
        val optimizedBitmap = loadAndOptimizeImage(imagePath, targetWidth, targetHeight, quality)
        optimizedBitmap?.let { bitmap ->
            // 缓存到内存和磁盘
            memoryOptimizationManager.cacheBitmap(cacheKey, bitmap)
            saveToDiskCache(cacheKey, bitmap)
        }
        
        return@withContext optimizedBitmap
    }
    
    /**
     * 预加载图片
     */
    fun preloadImages(imagePaths: List<String>, priority: LoadPriority = LoadPriority.LOW) {
        compressionScope.launch {
            imagePaths.forEach { imagePath ->
                if (isActive) {
                    try {
                        getOptimizedImage(imagePath)
                        if (priority == LoadPriority.LOW) {
                            delay(100) // 低优先级预加载时添加延迟
                        }
                    } catch (e: Exception) {
                        // 预加载失败不影响主流程
                    }
                }
            }
        }
    }
    
    /**
     * 清理缓存
     */
    suspend fun cleanCache(cleanLevel: CacheCleanLevel = CacheCleanLevel.NORMAL) = cacheMutex.withLock {
        when (cleanLevel) {
            CacheCleanLevel.LIGHT -> {
                // 轻度清理：只清理过期的磁盘缓存
                cleanExpiredDiskCache()
            }
            CacheCleanLevel.NORMAL -> {
                // 正常清理：清理部分内存缓存和过期磁盘缓存
                memoryOptimizationManager.performMemoryCleanup(MemoryCleanupLevel.NORMAL)
                cleanExpiredDiskCache()
            }
            CacheCleanLevel.AGGRESSIVE -> {
                // 激进清理：清理所有缓存
                memoryOptimizationManager.performMemoryCleanup(MemoryCleanupLevel.AGGRESSIVE)
                clearDiskCache()
            }
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): ImageCacheStatistics = cacheMutex.withLock {
        val diskCacheSize = calculateDiskCacheSize()
        val diskCacheFileCount = diskCacheDir.listFiles()?.size ?: 0
        
        return ImageCacheStatistics(
            memoryCacheSize = memoryOptimizationManager.getCacheStats().imageCacheSize,
            diskCacheSize = diskCacheSize,
            diskCacheFileCount = diskCacheFileCount,
            maxMemoryCacheSize = maxMemoryCacheSize,
            maxDiskCacheSize = maxDiskCacheSize
        )
    }
    
    /**
     * 加载并优化图片
     */
    private suspend fun loadAndOptimizeImage(
        imagePath: String,
        targetWidth: Int,
        targetHeight: Int,
        quality: ImageQuality
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) return@withContext null
            
            // 获取图片尺寸信息
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            // 计算采样率
            val sampleSize = calculateSampleSize(
                options.outWidth, options.outHeight,
                targetWidth, targetHeight, quality
            )
            
            // 加载图片
            val loadOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = quality.bitmapConfig
                inJustDecodeBounds = false
            }
            
            return@withContext BitmapFactory.decodeFile(imagePath, loadOptions)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 计算采样率
     */
    private fun calculateSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        quality: ImageQuality
    ): Int {
        var sampleSize = 1
        
        if (targetWidth > 0 && targetHeight > 0) {
            val halfHeight = originalHeight / 2
            val halfWidth = originalWidth / 2
            
            while ((halfHeight / sampleSize) >= targetHeight && (halfWidth / sampleSize) >= targetWidth) {
                sampleSize *= 2
            }
        }
        
        // 根据质量调整采样率
        return when (quality) {
            ImageQuality.LOW -> sampleSize * 2
            ImageQuality.MEDIUM -> sampleSize
            ImageQuality.HIGH -> max(1, sampleSize / 2)
        }
    }
    
    /**
     * 从磁盘缓存加载
     */
    private suspend fun loadFromDiskCache(cacheKey: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(diskCacheDir, cacheKey)
            if (cacheFile.exists()) {
                return@withContext BitmapFactory.decodeFile(cacheFile.absolutePath)
            }
        } catch (e: Exception) {
            // 加载失败
        }
        return@withContext null
    }
    
    /**
     * 保存到磁盘缓存
     */
    private suspend fun saveToDiskCache(cacheKey: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        try {
            val cacheFile = File(diskCacheDir, cacheKey)
            FileOutputStream(cacheFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            
            // 检查磁盘缓存大小
            if (calculateDiskCacheSize() > maxDiskCacheSize) {
                cleanOldestDiskCache()
            }
        } catch (e: Exception) {
            // 保存失败
        }
    }
    
    /**
     * 生成缓存键
     */
    private fun generateCacheKey(
        imagePath: String,
        targetWidth: Int,
        targetHeight: Int,
        quality: ImageQuality
    ): String {
        val input = "$imagePath-$targetWidth-$targetHeight-${quality.name}"
        val md5 = MessageDigest.getInstance("MD5")
        val digest = md5.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 计算磁盘缓存大小
     */
    private fun calculateDiskCacheSize(): Long {
        return diskCacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }
    
    /**
     * 清理过期的磁盘缓存
     */
    private suspend fun cleanExpiredDiskCache() = withContext(Dispatchers.IO) {
        val expireTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7天
        diskCacheDir.listFiles()?.forEach { file ->
            if (file.lastModified() < expireTime) {
                file.delete()
            }
        }
    }
    
    /**
     * 清理最旧的磁盘缓存
     */
    private suspend fun cleanOldestDiskCache() = withContext(Dispatchers.IO) {
        val files = diskCacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext
        val targetSize = maxDiskCacheSize * 0.8 // 清理到80%
        var currentSize = calculateDiskCacheSize()
        
        for (file in files) {
            if (currentSize <= targetSize) break
            currentSize -= file.length()
            file.delete()
        }
    }
    
    /**
     * 清空磁盘缓存
     */
    private suspend fun clearDiskCache() = withContext(Dispatchers.IO) {
        diskCacheDir.listFiles()?.forEach { it.delete() }
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        compressionScope.cancel()
    }
}

/**
 * 图片质量枚举
 */
enum class ImageQuality(val bitmapConfig: Bitmap.Config) {
    LOW(Bitmap.Config.RGB_565),
    MEDIUM(Bitmap.Config.ARGB_8888),
    HIGH(Bitmap.Config.ARGB_8888)
}

/**
 * 加载优先级
 */
enum class LoadPriority {
    LOW,
    NORMAL,
    HIGH
}

/**
 * 缓存清理级别
 */
enum class CacheCleanLevel {
    LIGHT,
    NORMAL,
    AGGRESSIVE
}

/**
 * 图片压缩配置
 */
data class ImageCompressionConfig(
    val maxWidth: Int = 1920,
    val maxHeight: Int = 1080,
    val jpegQuality: Int = 85,
    val enableWebP: Boolean = true
)

/**
 * 图片缓存统计信息
 */
data class ImageCacheStatistics(
    val memoryCacheSize: Int,
    val diskCacheSize: Long,
    val diskCacheFileCount: Int,
    val maxMemoryCacheSize: Int,
    val maxDiskCacheSize: Long
) {
    val memoryCacheUsageRatio: Float
        get() = memoryCacheSize.toFloat() / maxMemoryCacheSize
    
    val diskCacheUsageRatio: Float
        get() = diskCacheSize.toFloat() / maxDiskCacheSize
}