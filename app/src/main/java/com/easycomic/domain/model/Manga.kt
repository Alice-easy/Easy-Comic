package com.easycomic.domain.model

/**
 * 漫画领域模型
 */
data class Manga(
    val id: Long = 0,
    val title: String,
    val author: String = "",
    val description: String = "",
    val filePath: String,
    val fileUri: String? = null,
    val fileFormat: String = "",
    val fileSize: Long = 0,
    val pageCount: Int = 0,
    val currentPage: Int = 0,
    val coverImagePath: String? = null,
    val thumbnailPath: String? = null,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    val tags: List<String> = emptyList(),
    val lastRead: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
) {
    /**
     * 获取阅读进度百分比
     */
    val progressPercentage: Float
        get() = if (pageCount > 0) (currentPage.toFloat() / pageCount.toFloat()) * 100f else 0f
    
    /**
     * 获取格式化的文件大小
     */
    val formattedFileSize: String
        get() = formatFileSize(fileSize)
    
    /**
     * 是否已读完
     */
    val isCompleted: Boolean
        get() = readingStatus == ReadingStatus.COMPLETED || currentPage >= pageCount
    
    /**
     * 获取标签字符串
     */
    val tagsString: String
        get() = tags.joinToString(", ")
    
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
}