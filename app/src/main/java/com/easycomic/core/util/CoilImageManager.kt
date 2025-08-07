package com.easycomic.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import coil.decode.DecodeResult
import coil.decode.DecodeUtils
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.request.Options
import coil.size.Precision
import coil.size.Scale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custom interceptor for performance optimization
 * Handles memory management and image optimization
 */
@Singleton
class PerformanceInterceptor @Inject constructor(
    private val context: Context,
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache
) : Interceptor {
    
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        
        // Check memory cache first
        val memoryCacheKey = request.memoryCacheKey
        val cachedValue = memoryCache[memoryCacheKey]
        if (cachedValue != null) {
            return SuccessResult(cachedValue.bitmap, request, cachedValue.isSampled)
        }
        
        // Optimize image request
        val optimizedRequest = optimizeRequest(request)
        
        // Proceed with optimized request
        return chain.proceed(optimizedRequest).also { result ->
            if (result is SuccessResult) {
                // Cache the successful result
                cacheResult(result.bitmap, request, result.isSampled)
            }
        }
    }
    
    private fun optimizeRequest(request: ImageRequest): ImageRequest {
        val builder = request.newBuilder()
        
        // Apply aggressive downscaling for large images
        val dimensions = request.targetSize
        if (dimensions != null) {
            val maxDimension = maxOf(dimensions.width, dimensions.height)
            if (maxDimension > 2048) {
                builder.size(2048, 2048)
                builder.precision(Precision.INEXACT)
                builder.scale(Scale.FIT)
            }
        }
        
        // Use RGB565 for memory efficiency
        builder.allowRgb565(true)
        
        // Enable hardware acceleration where possible
        builder.allowHardware(true)
        
        // Disable crossfade for performance
        builder.crossfade(false)
        
        return builder.build()
    }
    
    private fun cacheResult(bitmap: Bitmap, request: ImageRequest, isSampled: Boolean) {
        val memoryCacheKey = request.memoryCacheKey
        if (memoryCacheKey != null) {
            val newMemoryCacheKey = MemoryCache.Key(memoryCacheKey.key, request.diskCacheKey)
            memoryCache.put(newMemoryCacheKey, MemoryCache.Value(bitmap, isSampled))
        }
    }
}

/**
 * Custom interceptor for memory management and debugging
 */
@Singleton
class MemoryDebugInterceptor @Inject constructor(
    private val context: Context
) : Interceptor {
    
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        
        // Log memory usage before loading
        logMemoryUsage("Before image load: ${request.data}")
        
        return try {
            val result = chain.proceed(request)
            
            // Log memory usage after loading
            logMemoryUsage("After image load: ${request.data}")
            
            if (result is SuccessResult) {
                logBitmapInfo(result.bitmap)
            }
            
            result
        } catch (e: Exception) {
            logMemoryUsage("Error loading image: ${request.data}")
            throw e
        }
    }
    
    private fun logMemoryUsage(message: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        
        android.util.Log.d("CoilMemory", "$message - Used: ${usedMemory}MB / ${maxMemory}MB")
    }
    
    private fun logBitmapInfo(bitmap: Bitmap) {
        val size = bitmap.byteCount / 1024 / 1024
        val config = bitmap.config
        android.util.Log.d("CoilMemory", 
            "Bitmap loaded - Size: ${size}MB, Config: $config, " +
            "Dimensions: ${bitmap.width}x${bitmap.height}")
    }
}

/**
 * Aggressive image decoder for memory optimization
 */
@Singleton
class AggressiveImageDecoder @Inject constructor(
    private val context: Context
) : Decoder {
    
    override suspend fun decode(): DecodeResult {
        val source = request.source
        val options = request.options
        
        return withContext(Dispatchers.IO) {
            try {
                // Get image dimensions first
                val bitmapOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                
                source.source().buffer().use { buffer ->
                    val peeked = buffer.peek()
                    val inputStream = peeked.inputStream()
                    BitmapFactory.decodeStream(inputStream, null, bitmapOptions)
                }
                
                // Calculate optimal sample size
                val targetSize = request.targetSize
                val sampleSize = calculateOptimalSampleSize(
                    bitmapOptions.outWidth,
                    bitmapOptions.outHeight,
                    targetSize?.width ?: 0,
                    targetSize?.height ?: 0
                )
                
                // Decode with sample size
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = if (options.allowRgb565) {
                        Bitmap.Config.RGB_565
                    } else {
                        Bitmap.Config.ARGB_8888
                    }
                    inMutable = false
                }
                
                val bitmap = source.source().buffer().use { buffer ->
                    val inputStream = buffer.inputStream()
                    BitmapFactory.decodeStream(inputStream, null, decodeOptions)
                }
                
                if (bitmap == null) {
                    throw IOException("Failed to decode image")
                }
                
                DecodeResult(bitmap, "image/*")
                
            } catch (e: Exception) {
                throw IOException("Failed to decode image", e)
            }
        }
    }
    
    private fun calculateOptimalSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        if (targetWidth <= 0 || targetHeight <= 0) {
            return 1
        }
        
        var inSampleSize = 1
        
        while (originalWidth / inSampleSize > targetWidth ||
               originalHeight / inSampleSize > targetHeight) {
            inSampleSize *= 2
        }
        
        return minOf(inSampleSize, 8) // Cap at 8 to prevent excessive pixelation
    }
}

/**
 * Custom image source for efficient file loading
 */
@Singleton
class EfficientImageSource @Inject constructor(
    private val context: Context
) : ImageSource.Factory {
    
    override fun create(data: Any, options: Options): ImageSource? {
        return when (data) {
            is String -> createStringSource(data, options)
            is File -> createFileSource(data, options)
            else -> null
        }
    }
    
    private fun createStringSource(path: String, options: Options): ImageSource? {
        val file = File(path)
        return if (file.exists()) {
            ImageSource(file)
        } else {
            null
        }
    }
    
    private fun createFileSource(file: File, options: Options): ImageSource? {
        return if (file.exists()) {
            ImageSource(file)
        } else {
            null
        }
    }
}

/**
 * Memory management utilities for image caching
 */
@Singleton
class ImageMemoryManager @Inject constructor(
    private val memoryCache: MemoryCache,
    private val diskCache: DiskCache
) {
    
    /**
     * Aggressively clear memory cache when memory is low
     */
    suspend fun clearMemoryCacheOnLowMemory() {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        // Clear cache if memory usage exceeds 70%
        if (usedMemory > maxMemory * 0.7) {
            memoryCache.clear()
            android.util.Log.d("ImageMemoryManager", "Cleared memory cache due to low memory")
        }
    }
    
    /**
     * Clear disk cache if it's too large
     */
    suspend fun clearDiskCacheIfNeeded() {
        val cacheDir = diskCache.directory
        val cacheSize = getDirectorySize(cacheDir)
        val maxSize = 512L * 1024 * 1024 // 512MB
        
        if (cacheSize > maxSize) {
            diskCache.clear()
            android.util.Log.d("ImageMemoryManager", "Cleared disk cache - size: ${cacheSize / 1024 / 1024}MB")
        }
    }
    
    /**
     * Preload images in background
     */
    suspend fun preloadImages(imagePaths: List<String>, imageLoader: ImageLoader) {
        withContext(Dispatchers.IO) {
            imagePaths.chunked(5).forEach { chunk ->
                chunk.forEach { path ->
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(path)
                            .size(256, 256) // Small preload size
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        
                        imageLoader.enqueue(request)
                    } catch (e: Exception) {
                        // Log but continue with other images
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun getDirectorySize(directory: File): Long {
        return directory.walkFiles().sumOf { it.length() }
    }
}

/**
 * Result types for image loading
 */
sealed class ImageResult
data class SuccessResult(val bitmap: Bitmap, val request: ImageRequest, val isSampled: Boolean) : ImageResult()
data class ErrorResult(val throwable: Throwable, val request: ImageRequest) : ImageResult()

/**
 * Interceptor interface for custom image loading logic
 */
interface Interceptor {
    suspend fun intercept(chain: Interceptor.Chain): ImageResult
    
    interface Chain {
        val request: ImageRequest
        suspend fun proceed(request: ImageRequest): ImageResult
    }
}