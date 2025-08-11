package com.easycomic.domain.model

/**
 * 阅读状态枚举 - 领域模型
 */
enum class ReadingStatus {
    UNREAD,     // 未读
    READING,    // 阅读中
    COMPLETED,  // 已完成
    PAUSED,     // 暂停
    DROPPED;    // 放弃
    
    /**
     * 获取状态的中文名称
     */
    fun getDisplayName(): String {
        return when (this) {
            UNREAD -> "未读"
            READING -> "阅读中"
            COMPLETED -> "已完成"
            PAUSED -> "暂停"
            DROPPED -> "放弃"
        }
    }
}