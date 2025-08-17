package com.easycomic.domain.model

/**
 * 书签领域模型
 */
data class Bookmark(
    val id: Long = 0,
    val mangaId: Long,
    val pageNumber: Int,
    val name: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * 获取格式化的创建时间
     */
    val formattedCreatedAt: String
        get() = formatTimestamp(createdAt)
    
    /**
     * 获取格式化的更新时间
     */
    val formattedUpdatedAt: String
        get() = formatTimestamp(updatedAt)
    
    /**
     * 是否为新创建的书签
     */
    val isNew: Boolean
        get() = System.currentTimeMillis() - createdAt < 24 * 60 * 60 * 1000 // 24小时内
    
    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            else -> {
                // 简单的日期格式化，实际项目中可以使用更好的日期格式化
                val date = java.util.Date(timestamp)
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                formatter.format(date)
            }
        }
    }
}