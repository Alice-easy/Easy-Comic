package com.easycomic.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.TypeConverters

@Entity(
    tableName = "manga",
    indices = [
        Index("title"),
        Index("filePath"),
        Index("isFavorite"),
        Index("dateAdded"),
        Index("progressPercentage")
    ]
)
@TypeConverters(Converters::class)
data class Manga(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val filePath: String,
    val coverImagePath: String? = null,
    val totalPages: Int = 0,
    val currentPage: Int = 0,
    val isFavorite: Boolean = false,
    val fileSize: Long = 0,
    val lastModified: Long = System.currentTimeMillis(),
    val dateAdded: Long = System.currentTimeMillis(),
    val format: String = "", // ZIP, RAR, CBZ, CBR
    val author: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(), // List of tags for filtering and categorization
    val rating: Float = 0f,
    val progressPercentage: Float = 0f,
    val lastReadTimestamp: Long = 0L // For quick access to reading history
)