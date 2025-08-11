package com.easycomic.data.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * 图片数据模型
 * 用于存储图片的二进制数据和相关元信息
 */
data class ImageData(
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val size: Int,
    val path: String = "",
    val mimeType: String = ""
) {
    
    /**
     * 获取格式化的文件大小
     */
    val formattedSize: String
        get() = formatFileSize(size.toLong())
    
    /**
     * 获取图片的宽高比
     */
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 1f
    
    /**
     * 转换为Bitmap
     */
    fun toBitmap(): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }
    
    /**
     * 压缩图片数据
     * @param quality 压缩质量 (0-100)
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的ImageData
     */
    fun compress(
        quality: Int = 85,
        maxWidth: Int? = null,
        maxHeight: Int? = null
    ): ImageData {
        val originalBitmap = toBitmap()
        
        // 计算目标尺寸
        val (targetWidth, targetHeight) = calculateTargetSize(
            originalBitmap.width,
            originalBitmap.height,
            maxWidth,
            maxHeight
        )
        
        // 缩放图片
        val scaledBitmap = if (targetWidth != originalBitmap.width || targetHeight != originalBitmap.height) {
            Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
        } else {
            originalBitmap
        }
        
        // 压缩图片
        val outputStream = ByteArrayOutputStream()
        val compressFormat = when (format) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
        }
        
        scaledBitmap.compress(compressFormat, quality, outputStream)
        val compressedData = outputStream.toByteArray()
        
        // 清理资源
        if (scaledBitmap != originalBitmap) {
            scaledBitmap.recycle()
        }
        originalBitmap.recycle()
        
        return ImageData(
            data = compressedData,
            width = targetWidth,
            height = targetHeight,
            format = format,
            size = compressedData.size,
            path = path,
            mimeType = mimeType
        )
    }
    
    /**
     * 计算目标尺寸
     */
    private fun calculateTargetSize(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int?,
        maxHeight: Int?
    ): Pair<Int, Int> {
        if (maxWidth == null && maxHeight == null) {
            return Pair(originalWidth, originalHeight)
        }
        
        val originalRatio = originalWidth.toFloat() / originalHeight.toFloat()
        
        var targetWidth = originalWidth
        var targetHeight = originalHeight
        
        if (maxWidth != null && originalWidth > maxWidth) {
            targetWidth = maxWidth
            targetHeight = (targetWidth / originalRatio).toInt()
        }
        
        if (maxHeight != null && targetHeight > maxHeight) {
            targetHeight = maxHeight
            targetWidth = (targetHeight * originalRatio).toInt()
        }
        
        return Pair(targetWidth, targetHeight)
    }
    
    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            else -> "${size / (1024 * 1024 * 1024)} GB"
        }
    }
    
    companion object {
        /**
         * 从输入流创建ImageData
         */
        fun fromInputStream(
            inputStream: InputStream,
            path: String = "",
            mimeType: String = ""
        ): ImageData {
            val buffer = inputStream.readBytes()
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(buffer, 0, buffer.size, options)
            
            val format = when {
                mimeType.contains("jpeg", ignoreCase = true) || 
                mimeType.contains("jpg", ignoreCase = true) -> ImageFormat.JPEG
                mimeType.contains("png", ignoreCase = true) -> ImageFormat.PNG
                mimeType.contains("webp", ignoreCase = true) -> ImageFormat.WEBP
                else -> ImageFormat.JPEG // 默认格式
            }
            
            return ImageData(
                data = buffer,
                width = options.outWidth,
                height = options.outHeight,
                format = format,
                size = buffer.size,
                path = path,
                mimeType = mimeType
            )
        }
        
        /**
         * 从Bitmap创建ImageData
         */
        fun fromBitmap(
            bitmap: Bitmap,
            format: ImageFormat = ImageFormat.JPEG,
            quality: Int = 85,
            path: String = "",
            mimeType: String = ""
        ): ImageData {
            val outputStream = ByteArrayOutputStream()
            val compressFormat = when (format) {
                ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
                ImageFormat.PNG -> Bitmap.CompressFormat.PNG
                ImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
            }
            
            bitmap.compress(compressFormat, quality, outputStream)
            val data = outputStream.toByteArray()
            
            return ImageData(
                data = data,
                width = bitmap.width,
                height = bitmap.height,
                format = format,
                size = data.size,
                path = path,
                mimeType = mimeType.ifEmpty { when (format) {
                    ImageFormat.JPEG -> "image/jpeg"
                    ImageFormat.PNG -> "image/png"
                    ImageFormat.WEBP -> "image/webp"
                } }
            )
        }
    }
}

/**
 * 图片格式枚举
 */
enum class ImageFormat {
    JPEG,
    PNG,
    WEBP
}