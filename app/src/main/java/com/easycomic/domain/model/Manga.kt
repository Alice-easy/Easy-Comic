package com.easycomic.domain.model

data class Manga(
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
    val format: String = "",
    val author: String? = null,
    val description: String? = null,
    val tags: List<String> = emptyList(),
    val rating: Float = 0f,
    val progressPercentage: Float = 0f,
    val readingHistory: ReadingHistory? = null,
    val bookmarks: List<Bookmark> = emptyList()
)