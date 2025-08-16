package com.easycomic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * 阅读历史实体类
 */
@Entity(
    tableName = "reading_history",
    foreignKeys = [
        ForeignKey(
            entity = MangaEntity::class,
            parentColumns = ["id"],
            childColumns = ["manga_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("manga_id"),
        Index("read_at"),
        Index("reading_duration")
    ]
)
data class ReadingHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "manga_id")
    val mangaId: Long,
    
    @ColumnInfo(name = "page_number")
    val pageNumber: Int,
    
    @ColumnInfo(name = "progress_percentage")
    val progressPercentage: Float = 0f,
    
    @ColumnInfo(name = "reading_duration")
    val readingDuration: Long = 0, // 阅读时长（毫秒）
    
    @ColumnInfo(name = "read_at")
    val readAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "session_id")
    val sessionId: String = ""
)