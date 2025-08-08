package com.easycomic.domain.repository

import com.easycomic.domain.model.Manga
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    fun observeAll(): Flow<List<Manga>>
    suspend fun getById(id: Long): Manga?
    suspend fun add(manga: Manga): Long
    suspend fun update(manga: Manga)
    suspend fun delete(id: Long)
    suspend fun updateProgress(id: Long, page: Int, progress: Float, lastRead: Long)
    suspend fun setFavorite(id: Long, favorite: Boolean)
}
