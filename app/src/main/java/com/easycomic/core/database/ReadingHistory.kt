package com.easycomic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "reading_history",
    foreignKeys = [
        ForeignKey(
            entity = Manga::class,
            parentColumns = ["id"],
            childColumns = ["mangaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("mangaId"),
        Index("lastReadTimestamp"),
        Index("isCompleted")
    ]
)
data class ReadingHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mangaId: Long,
    val lastPageRead: Int = 0,
    val totalPages: Int = 0,
    val readingTime: Long = 0, // Total reading time in milliseconds
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val readingSessions: Int = 0, // Number of reading sessions
    val averagePagesPerSession: Float = 0f,
    val readingProgress: Float = 0f, // Normalized progress (0.0 to 1.0)
    val sessionStartTime: Long = 0L, // For tracking current session
    val notes: String? = null // User notes about reading experience
)