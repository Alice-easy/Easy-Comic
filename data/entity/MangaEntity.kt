package com.easycomic.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 漫画实体类
 * @Entity 定义数据库表结构，严格对应 README.md 中的设计
 */
@Entity(
    tableName = "manga",
    indices = [
        Index(value = ["title"], name = "idx_manga_title"),
        Index(value = ["last_read"], name = "idx_manga_last_read", orders = [Index.Order.DESC]),
        Index(value = ["is_favorite", "last_read"], name = "idx_manga_favorite", orders = [Index.Order.ASC, Index.Order.DESC])
    ]
)
data class MangaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author", defaultValue = "")
    val author: String? = "",

    @ColumnInfo(name = "description", defaultValue = "")
    val description: String? = "",

    @ColumnInfo(name = "file_path")
    val filePath: String,

    @ColumnInfo(name = "file_size")
    val fileSize: Long,

    @ColumnInfo(name = "format")
    val format: String,

    @ColumnInfo(name = "cover_path")
    val coverPath: String? = null,

    @ColumnInfo(name = "page_count", defaultValue = "0")
    val pageCount: Int = 0,

    @ColumnInfo(name = "current_page", defaultValue = "1")
    val currentPage: Int = 1,

    @ColumnInfo(name = "reading_progress", defaultValue = "0.0")
    val readingProgress: Float = 0.0f,

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "is_completed", defaultValue = "0")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "date_added")
    val dateAdded: Long,

    @ColumnInfo(name = "last_read", defaultValue = "0")
    val lastRead: Long = 0,

    @ColumnInfo(name = "reading_time", defaultValue = "0")
    val readingTime: Long = 0,

    @ColumnInfo(name = "rating", defaultValue = "0.0")
    val rating: Float = 0.0f,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
