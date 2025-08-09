package com.easycomic.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.decodeBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * 简化的图像加载器，支持大图像的区域解码
 */
class ImageLoader {
    
    // 内存缓存
    private val bitmapCache = mutableMapOf<String, Bitmap>()
    
    /**
     * 加载图像并优化内存使用
     */
    suspend fun loadImage(
        imageData: ByteArray,
        reqWidth: Int = 0,
        reqHeight: Int = 0,
        useRegionDecoder: Boolean = true
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            Timber.d("加载图像，大小: ${imageData.size} bytes")
            
            // 计算采样率以减少内存使用
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(imageData, 0, imageData.size, this)
                
                if (reqWidth > 0 && reqHeight > 0) {
                    inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)
                } else {
                    // 默认缩小到屏幕尺寸的1/4
                    val targetWidth = 1080 / 4
                    val targetHeight = 1920 / 4
                    inSampleSize = calculateInSampleSize(outWidth, outHeight, targetWidth, targetHeight)
                }
                
                inJustDecodeBounds = false
                inPreferredConfig = Bitmap.Config.RGB_565 // 减少内存使用
            }
            
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size, options)
            
            if (bitmap != null) {
                Timber.d("图像加载成功: ${bitmap.width}x${bitmap.height}")
                Result.success(bitmap)
            } else {
                Result.failure(Exception("图像解码失败"))
            }
            
        } catch (e: Exception) {
            Timber.e(e, "加载图像失败")
            Result.failure(e)
        }
    }
    
    /**
     * 加载图像的一部分（简化版本）
     */
    suspend fun loadImageRegion(
        imageData: ByteArray,
        rect: Rect,
        reqWidth: Int,
        reqHeight: Int
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            Timber.d("加载图像区域: $rect, 目标尺寸: ${reqWidth}x${reqHeight}")
            
            // 简化版本：先加载整个图像，然后裁剪
            loadImage(imageData).map { bitmap ->
                // 创建裁剪后的图像
                if (rect.left >= 0 && rect.top >= 0 && 
                    rect.right <= bitmap.width && rect.bottom <= bitmap.height) {
                    Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
                } else {
                    bitmap // 如果区域无效，返回原图
                }
            }
            
        } catch (e: Exception) {
            Timber.e(e, "加载图像区域失败")
            Result.failure(e)
        }
    }
    
    /**
     * 缩放图像到指定尺寸
     */
    suspend fun scaleBitmap(
        bitmap: Bitmap,
        targetWidth: Int,
        targetHeight: Int
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        try {
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
            Result.success(scaledBitmap)
        } catch (e: Exception) {
            Timber.e(e, "缩放图像失败")
            Result.failure(e)
        }
    }
    
    /**
     * 加载图像用于Compose显示
     */
    suspend fun loadForCompose(
        imageData: ByteArray
    ): Result<androidx.compose.ui.graphics.ImageBitmap> = withContext(Dispatchers.Default) {
        try {
            loadImage(imageData).map { bitmap ->
                bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            Timber.e(e, "加载Compose图像失败")
            Result.failure(e)
        }
    }
    
    /**
     * 清理内存缓存
     */
    fun clearCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
        System.gc()
        Timber.d("图像缓存已清理")
    }
    
    /**
     * 计算适当的采样率
     */
    private fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight: Int = srcHeight / 2
            val halfWidth: Int = srcWidth / 2
            
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    /**
     * 获取内存使用情况
     */
    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val usedPercent = (usedMemory.toFloat() / maxMemory.toFloat() * 100).toInt()
        
        return "内存使用: ${usedMemory / 1024 / 1024}MB / ${maxMemory / 1024 / 1024}MB ($usedPercent%)"
    }
    
    /**
     * 获取缓存信息
     */
    fun getCacheInfo(): String {
        val cacheSize = bitmapCache.size
        val totalPixels = bitmapCache.values.sumOf { it.width * it.height }
        
        return "缓存: $cacheSize 项, 总像素: $totalPixels"
    }
}