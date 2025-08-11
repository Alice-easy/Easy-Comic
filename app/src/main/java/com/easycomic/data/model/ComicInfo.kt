package com.easycomic.data.model

/**
 * 漫画信息数据模型
 * 用于存储解析后的漫画文件信息
 */
data class ComicInfo(
    val title: String,
    val filePath: String,
    val fileUri: String,
    val fileFormat: String,
    val fileSize: Long,
    val pageCount: Int = 0,
    val entries: List<ComicEntry> = emptyList(),
    val coverEntry: ComicEntry? = null,
    val author: String = "",
    val description: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
) {
    
    /**
     * 获取格式化的文件大小
     */
    val formattedFileSize: String
        get() = formatFileSize(fileSize)
    
    /**
     * 获取文件扩展名
     */
    val fileExtension: String
        get() = filePath.substringAfterLast('.', "").lowercase()
    
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
    
    /**
     * 漫画条目信息
     */
    data class ComicEntry(
        val name: String,
        val path: String,
        val size: Long,
        val compressedSize: Long,
        val lastModified: Long,
        val index: Int,
        val isImage: Boolean = true,
        val isCover: Boolean = false
    ) {
        
        /**
         * 获取文件扩展名
         */
        val extension: String
            get() = path.substringAfterLast('.', "").lowercase()
        
        /**
         * 获取格式化的文件大小
         */
        val formattedSize: String
            get() = formatFileSize(size)
        
        /**
         * 获取格式化的压缩文件大小
         */
        val formattedCompressedSize: String
            get() = formatFileSize(compressedSize)
        
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
}