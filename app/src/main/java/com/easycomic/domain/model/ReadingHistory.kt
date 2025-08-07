package com.easycomic.domain.model

data class ReadingHistory(
    val id: Long = 0,
    val mangaId: Long,
    val lastPageRead: Int = 0,
    val totalPages: Int = 0,
    val readingTime: Long = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val readingSessions: Int = 0,
    val averagePagesPerSession: Float = 0f
)