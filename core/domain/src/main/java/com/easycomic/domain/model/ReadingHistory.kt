package com.easycomic.domain.model

/**
 * 阅读历史领域模型
 */
data class ReadingHistory(
    val id: Long = 0,
    val mangaId: Long,
    val pageNumber: Int,
    val progressPercentage: Float = 0f,
    val readingDuration: Long = 0, // 阅读时长（毫秒）
    val readAt: Long = System.currentTimeMillis(),
    val sessionId: String = ""
) {
    /**
     * 获取格式化的阅读时长
     */
    val formattedReadingDuration: String
        get() = formatDuration(readingDuration)
    
    /**
     * 获取格式化的阅读时间
     */
    val formattedReadAt: String
        get() = formatTimestamp(readAt)
    
    /**
     * 是否为今天的阅读记录
     */
    val isToday: Boolean
        get() = isDateToday(readAt)
    
    /**
     * 是否为本周的阅读记录
     */
    val isThisWeek: Boolean
        get() = isDateThisWeek(readAt)
    
    /**
     * 格式化时长
     */
    private fun formatDuration(duration: Long): String {
        return when {
            duration < 60 * 1000 -> "${duration / 1000}秒"
            duration < 60 * 60 * 1000 -> "${duration / (60 * 1000)}分钟"
            duration < 24 * 60 * 60 * 1000 -> "${duration / (60 * 60 * 1000)}小时"
            else -> {
                val hours = duration / (60 * 60 * 1000)
                val minutes = (duration % (60 * 60 * 1000)) / (60 * 1000)
                "${hours}小时${minutes}分钟"
            }
        }
    }
    
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
            diff < 2 * 24 * 60 * 60 * 1000 -> "昨天"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
            else -> {
                val date = java.util.Date(timestamp)
                val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                formatter.format(date)
            }
        }
    }
    
    /**
     * 判断是否为今天
     */
    private fun isDateToday(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val today = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val year = calendar.get(java.util.Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val targetDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val targetYear = calendar.get(java.util.Calendar.YEAR)
        
        return today == targetDay && year == targetYear
    }
    
    /**
     * 判断是否为本周
     */
    private fun isDateThisWeek(timestamp: Long): Boolean {
        val calendar = java.util.Calendar.getInstance()
        val currentWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        calendar.timeInMillis = timestamp
        val targetWeek = calendar.get(java.util.Calendar.WEEK_OF_YEAR)
        val targetYear = calendar.get(java.util.Calendar.YEAR)
        
        return currentWeek == targetWeek && currentYear == targetYear
    }
}