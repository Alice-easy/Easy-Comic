package com.easycomic.model

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * 简化的漫画数据模型
 */
data class Comic(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val filePath: String,
    val fileUri: Uri?,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val coverImage: ByteArray? = null,
    val isFavorite: Boolean = false,
    val lastRead: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis()
)

/**
 * 漫画页面数据
 */
data class ComicPage(
    val index: Int,
    val imageData: ByteArray?,
    val imageFormat: String = "jpg",
    val width: Int = 0,
    val height: Int = 0
)

/**
 * 文件解析结果
 */
sealed class ParseResult {
    data class Success(val comic: Comic, val pages: List<ComicPage>) : ParseResult()
    data class Error(val message: String, val exception: Throwable? = null) : ParseResult()
    data class Loading(val progress: Float = 0f) : ParseResult()
}

/**
 * 支持的文件类型
 */
object ComicFileType {
    private val SUPPORTED_ZIP = setOf("zip", "cbz")
    private val SUPPORTED_RAR = setOf("rar", "cbr")
    private val SUPPORTED_IMAGES = setOf("jpg", "jpeg", "png", "webp", "gif")
    
    val ALL_ARCHIVE_TYPES = SUPPORTED_ZIP + SUPPORTED_RAR
    val ALL_SUPPORTED_TYPES = ALL_ARCHIVE_TYPES + SUPPORTED_IMAGES
    
    fun isSupportedArchive(filePath: String): Boolean {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return extension in ALL_ARCHIVE_TYPES
    }
    
    fun isImageFile(filePath: String): Boolean {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return extension in SUPPORTED_IMAGES
    }
}