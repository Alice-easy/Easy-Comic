package com.easycomic.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.ColumnInfo

/**
 * 书签实体类
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
        Index("manga_id"),
        Index("page_number"),
        Index("created_at")
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
    
    @ColumnInfo(name = "name")
    val name: String = "",
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)