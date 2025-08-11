package com.easycomic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * 漫画实体类
 * @Entity 定义数据库表结构
 */
@Entity(
    tableName = "manga",
    indices = [
        Index("title"),
        Index("author"),
        Index("last_read"),
        Index("date_added"),
        Index("is_favorite")
    ]
)
data class MangaEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "author")
    val author: String = "",
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "file_path")
    val filePath: String,
    
    @ColumnInfo(name = "file_uri")
    val fileUri: String? = null,
    
    @ColumnInfo(name = "file_format")
    val fileFormat: String = "",
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0,
    
    @ColumnInfo(name = "page_count")
    val pageCount: Int = 0,
    
    @ColumnInfo(name = "current_page")
    val currentPage: Int = 0,
    
    @ColumnInfo(name = "cover_image_path")
    val coverImagePath: String? = null,
    
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null,
    
    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
    
    @ColumnInfo(name = "reading_status")
    val readingStatus: ReadingStatus = ReadingStatus.UNREAD,
    
    @ColumnInfo(name = "tags")
    val tags: String = "",
    
    @ColumnInfo(name = "last_read")
    val lastRead: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "date_modified")
    val dateModified: Long = System.currentTimeMillis()
)

/**
 * 阅读状态枚举
 */
enum class ReadingStatus {
    UNREAD,     // 未读
    READING,    // 阅读中
    COMPLETED,  // 已完成
    PAUSED,     // 暂停
    DROPPED     // 放弃
}