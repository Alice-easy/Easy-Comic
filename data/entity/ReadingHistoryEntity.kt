package com.easycomic.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 阅读历史实体类
 * 严格对应 README.md 中的设计
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
        Index(value = ["manga_id"], name = "idx_history_manga_id"),
        Index(value = ["session_start"], name = "idx_history_session")
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

    @ColumnInfo(name = "reading_time")
    val readingTime: Long, // 本次阅读时间（秒）

    @ColumnInfo(name = "session_start")
    val sessionStart: Long,

    @ColumnInfo(name = "session_end")
    val sessionEnd: Long,

    @ColumnInfo(name = "reading_speed")
    val readingSpeed: Float?, // 页/分钟

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
