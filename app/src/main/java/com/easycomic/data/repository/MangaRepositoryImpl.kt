package com.easycomic.data.repository

import com.easycomic.data.database.dao.MangaDao
import com.easycomic.data.database.entity.MangaEntity
import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MangaRepositoryImpl @Inject constructor(
    private val mangaDao: MangaDao
) : MangaRepository {

    override fun observeAll(): Flow<List<Manga>> = mangaDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): Manga? = mangaDao.getById(id)?.toDomain()

    override suspend fun add(manga: Manga): Long = mangaDao.insert(manga.toEntity())

    override suspend fun update(manga: Manga) = mangaDao.update(manga.toEntity(requireId = true))

    override suspend fun delete(id: Long) {
        mangaDao.getById(id)?.let { mangaDao.delete(it) }
    }

    override suspend fun updateProgress(id: Long, page: Int, progress: Float, lastRead: Long) {
        mangaDao.updateProgress(id, page, progress, lastRead, updatedAt = System.currentTimeMillis())
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        mangaDao.updateFavorite(id, favorite, updatedAt = System.currentTimeMillis())
    }
}

private fun MangaEntity.toDomain() = Manga(
    id = id,
    title = title,
    author = author,
    description = description,
    filePath = filePath,
    fileSize = fileSize,
    format = format,
    coverPath = coverPath,
    pageCount = pageCount,
    currentPage = currentPage,
    readingProgress = readingProgress,
    isFavorite = isFavorite,
    isCompleted = isCompleted,
    dateAdded = dateAdded,
    lastRead = lastRead,
    readingTime = readingTime,
    rating = rating,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun Manga.toEntity(requireId: Boolean = false) = MangaEntity(
    id = if (requireId) id else 0L,
    title = title,
    author = author,
    description = description,
    filePath = filePath,
    fileSize = fileSize,
    format = format,
    coverPath = coverPath,
    pageCount = pageCount,
    currentPage = currentPage,
    readingProgress = readingProgress,
    isFavorite = isFavorite,
    isCompleted = isCompleted,
    dateAdded = dateAdded,
    lastRead = lastRead,
    readingTime = readingTime,
    rating = rating,
    createdAt = createdAt,
    updatedAt = updatedAt
)
