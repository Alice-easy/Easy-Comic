package com.easycomic.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 书签实体类
 * 严格对应 README.md 中的设计
 */
@Entity(
    tableName = "bookmark",
    foreignKeys = [
        ForeignKey(
            entity = MangaEntity::class,
            parentColumns = ["id"],
            childColumns = ["manga_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["manga_id"], name = "idx_bookmark_manga_id"),
        Index(value = ["manga_id", "page_number"], unique = true)
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "manga_id")
    val mangaId: Long,

    @ColumnInfo(name = "page_number")
    val pageNumber: Int,

    @ColumnInfo(name = "bookmark_name")
    val bookmarkName: String?,

    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
