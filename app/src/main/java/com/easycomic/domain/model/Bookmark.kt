package com.easycomic.domain.model

data class Bookmark(
    val id: Long = 0,
    val mangaId: Long,
    val page: Int,
    val name: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)