package com.easycomic.domain.model

data class Manga(
    val id: Long = 0L,
    val title: String,
    val author: String? = null,
    val description: String? = null,
    val filePath: String,
    val fileSize: Long,
    val format: String,
    val coverPath: String? = null,
    val pageCount: Int = 0,
    val currentPage: Int = 1,
    val readingProgress: Float = 0f,
    val isFavorite: Boolean = false,
    val isCompleted: Boolean = false,
    val dateAdded: Long,
    val lastRead: Long? = null,
    val readingTime: Long = 0L,
    val rating: Float = 0f,
    val createdAt: Long,
    val updatedAt: Long
)
