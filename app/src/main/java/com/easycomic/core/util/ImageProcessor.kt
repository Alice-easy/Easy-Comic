package com.easycomic.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import androidx.exifinterface.media.ExifInterface
import coil.ImageLoader
import coil.decode.BitmapFactoryDecoder
import coil.request.ImageRequest
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor(
    private val context: Context
) {
    
    private val imageLoader = ImageLoader.Builder(context)
        .components {
            add(BitmapFactoryDecoder.Factory())
        }
        .build()
    
    // Cache for region decoders to avoid repeated file access
    private val regionDecoderCache = mutableMapOf<String, BitmapRegionDecoder>()
    private val decoderLock = Any()
    
    suspend fun loadAndOptimizeImage(
        imagePath: String,
        maxWidth: Int = 1080,
        maxHeight: Int = 1920,
        quality: Int = 85
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(imagePath)
            if (!originalFile.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            // Get original bitmap dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            // Calculate sample size
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
            
            // Decode with sample size
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            
            val bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))
            
            // Handle EXIF rotation
            val rotatedBitmap = rotateBitmapIfNeeded(bitmap, imagePath)
            
            // Create optimized file
            val optimizedFile = createOptimizedFile(rotatedBitmap, quality)
            
            // Clean up
            if (rotatedBitmap != bitmap) {
                bitmap.recycle()
            }
            rotatedBitmap.recycle()
            
            Result.success(optimizedFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        var inSampleSize = 1
        
        if (originalHeight > maxHeight || originalWidth > maxWidth) {
            val halfHeight: Int = originalHeight / 2
            val halfWidth: Int = originalWidth / 2
            
            while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    private suspend fun rotateBitmapIfNeeded(bitmap: Bitmap, imagePath: String): Bitmap = withContext(Dispatchers.IO) {
        try {
            val exif = ExifInterface(imagePath)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postRotate(90f)
                    matrix.postScale(-1f, 1f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postRotate(-90f)
                    matrix.postScale(-1f, 1f)
                }
            }
            
            if (matrix.isIdentity) {
                bitmap
            } else {
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (e: Exception) {
            bitmap
        }
    }
    
    private suspend fun createOptimizedFile(bitmap: Bitmap, quality: Int): File = withContext(Dispatchers.IO) {
        val outputFile = File(context.cacheDir, "optimized_${System.currentTimeMillis()}.jpg")
        
        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        
        outputFile
    }
    
    suspend fun createThumbnail(
        imagePath: String,
        width: Int = 200,
        height: Int = 300
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val originalFile = File(imagePath)
            if (!originalFile.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            // Load and scale bitmap
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            // Calculate sample size for thumbnail
            val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, width, height)
            
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            
            var bitmap = BitmapFactory.decodeFile(imagePath, decodeOptions)
                ?: return@withContext Result.failure(Exception("Failed to decode image"))
            
            // Handle EXIF rotation
            bitmap = rotateBitmapIfNeeded(bitmap, imagePath)
            
            // Scale to exact dimensions
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
            
            // Create thumbnail file
            val thumbnailFile = File(context.cacheDir, "thumb_${System.currentTimeMillis()}.jpg")
            FileOutputStream(thumbnailFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            
            // Clean up
            scaledBitmap.recycle()
            if (bitmap != scaledBitmap) {
                bitmap.recycle()
            }
            
            Result.success(thumbnailFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getBitmapFromPath(imagePath: String): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap != null) {
                Result.success(bitmap)
            } else {
                Result.failure(Exception("Failed to decode bitmap"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun createImageRequest(imagePath: String): ImageRequest {
        return ImageRequest.Builder(context)
            .data(imagePath)
            .crossfade(true)
            .allowHardware(true)
            .build()
    }
    
    suspend fun clearImageCache() {
        withContext(Dispatchers.IO) {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("optimized_") || file.name.startsWith("thumb_")) {
                    file.delete()
                }
            }
        }
    }
    
    /**
     * Load a region of a large image using BitmapRegionDecoder
     * This is memory efficient for very large images
     */
    suspend fun loadImageRegion(
        imagePath: String,
        region: Rect,
        sampleSize: Int = 1,
        config: Bitmap.Config = Bitmap.Config.RGB_565
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            // Get or create region decoder
            val decoder = getRegionDecoder(imagePath)
            
            // Decode only the requested region
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = config
                inMutable = true
            }
            
            val bitmap = decoder.decodeRegion(region, options)
                ?: return@withContext Result.failure(Exception("Failed to decode image region"))
            
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load a tile of an image for zooming and panning
     */
    suspend fun loadImageTile(
        imagePath: String,
        tileSize: Int = 512,
        x: Int = 0,
        y: Int = 0,
        sampleSize: Int = 1
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            // Get image dimensions
            val decoder = getRegionDecoder(imagePath)
            val imageWidth = decoder.width
            val imageHeight = decoder.height
            
            // Calculate tile region
            val tileX = (x * tileSize * sampleSize).coerceAtLeast(0)
            val tileY = (y * tileSize * sampleSize).coerceAtLeast(0)
            val tileWidth = tileSize.coerceAtMost(imageWidth - tileX)
            val tileHeight = tileSize.coerceAtMost(imageHeight - tileY)
            
            if (tileWidth <= 0 || tileHeight <= 0) {
                return@withContext Result.failure(Exception("Invalid tile position"))
            }
            
            val region = Rect(tileX, tileY, tileX + tileWidth, tileY + tileHeight)
            
            // Decode the tile
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
                inMutable = true
            }
            
            val bitmap = decoder.decodeRegion(region, options)
                ?: return@withContext Result.failure(Exception("Failed to decode image tile"))
            
            Result.success(bitmap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get image dimensions without loading the full bitmap
     */
    suspend fun getImageDimensions(imagePath: String): Result<ImageDimensions> = withContext(Dispatchers.IO) {
        try {
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            // First try to get dimensions using BitmapFactory (faster)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)
            
            if (options.outWidth > 0 && options.outHeight > 0) {
                return@withContext Result.success(
                    ImageDimensions(options.outWidth, options.outHeight)
                )
            }
            
            // Fallback to BitmapRegionDecoder for very large files
            val decoder = getRegionDecoder(imagePath)
            Result.success(ImageDimensions(decoder.width, decoder.height))
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a tiled view of a large image
     * Returns a list of tiles that can be loaded on demand
     */
    suspend fun createTileLayout(
        imagePath: String,
        tileSize: Int = 512,
        maxSampleSize: Int = 4
    ): Result<TileLayout> = withContext(Dispatchers.IO) {
        try {
            val dimensions = getImageDimensions(imagePath).getOrThrow()
            val tiles = mutableListOf<TileInfo>()
            
            // Calculate optimal sample size
            val maxDimension = maxOf(dimensions.width, dimensions.height)
            val optimalSampleSize = calculateOptimalSampleSize(maxDimension, tileSize)
            
            // Generate tile grid
            val tilesX = (dimensions.width + tileSize - 1) / tileSize
            val tilesY = (dimensions.height + tileSize - 1) / tileSize
            
            for (y in 0 until tilesY) {
                for (x in 0 until tilesX) {
                    tiles.add(
                        TileInfo(
                            x = x,
                            y = y,
                            tileSize = tileSize,
                            sampleSize = optimalSampleSize,
                            region = Rect(
                                x * tileSize,
                                y * tileSize,
                                minOf((x + 1) * tileSize, dimensions.width),
                                minOf((y + 1) * tileSize, dimensions.height)
                            )
                        )
                    )
                }
            }
            
            Result.success(
                TileLayout(
                    imageWidth = dimensions.width,
                    imageHeight = dimensions.height,
                    tileSize = tileSize,
                    tilesX = tilesX,
                    tilesY = tilesY,
                    tiles = tiles,
                    optimalSampleSize = optimalSampleSize
                )
            )
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load a low-resolution preview of a very large image
     */
    suspend fun loadLowResPreview(
        imagePath: String,
        maxWidth: Int = 512,
        maxHeight: Int = 512
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            val dimensions = getImageDimensions(imagePath).getOrThrow()
            
            // Calculate sample size to fit within max dimensions
            val sampleSize = calculateSampleSize(
                dimensions.width, dimensions.height,
                maxWidth, maxHeight
            )
            
            // Load the entire image at reduced resolution
            val file = File(imagePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("Image file not found"))
            }
            
            val decoder = getRegionDecoder(imagePath)
            val region = Rect(0, 0, dimensions.width, dimensions.height)
            
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
                inMutable = false
            }
            
            val bitmap = decoder.decodeRegion(region, options)
                ?: return@withContext Result.failure(Exception("Failed to decode preview"))
            
            Result.success(bitmap)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up region decoder cache
     */
    suspend fun cleanupRegionDecoders() {
        withContext(Dispatchers.IO) {
            synchronized(decoderLock) {
                regionDecoderCache.values.forEach { decoder ->
                    try {
                        decoder.recycle()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                regionDecoderCache.clear()
            }
        }
    }
    
    // Private helper methods
    
    private suspend fun getRegionDecoder(imagePath: String): BitmapRegionDecoder = withContext(Dispatchers.IO) {
        synchronized(decoderLock) {
            regionDecoderCache.getOrPut(imagePath) {
                val file = File(imagePath)
                if (!file.exists()) {
                    throw Exception("Image file not found")
                }
                
                // Use RandomAccessFile for better performance with large files
                RandomAccessFile(file, "r").use { raf ->
                    val fd = raf.fd
                    BitmapRegionDecoder.newInstance(fd, false)
                }
            }
        }
    }
    
    private fun calculateOptimalSampleSize(imageDimension: Int, tileSize: Int): Int {
        var sampleSize = 1
        while (imageDimension / (sampleSize * tileSize) > 2 && sampleSize < 4) {
            sampleSize *= 2
        }
        return sampleSize
    }
    
    /**
     * Data class for image dimensions
     */
    data class ImageDimensions(
        val width: Int,
        val height: Int
    )
    
    /**
     * Data class for tile information
     */
    data class TileInfo(
        val x: Int,
        val y: Int,
        val tileSize: Int,
        val sampleSize: Int,
        val region: Rect
    )
    
    /**
     * Data class for tile layout
     */
    data class TileLayout(
        val imageWidth: Int,
        val imageHeight: Int,
        val tileSize: Int,
        val tilesX: Int,
        val tilesY: Int,
        val tiles: List<TileInfo>,
        val optimalSampleSize: Int
    )
}