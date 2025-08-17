package com.easycomic.data.repository

import android.graphics.Bitmap
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 漫画仓库实现类
 */
class MangaRepositoryImpl(
    private val mangaDao: MangaDao
) : MangaRepository {
    
    override fun getAllManga(): Flow<List<Manga>> {
        return mangaDao.getAllManga().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getMangaById(id: Long): Manga? {
        return mangaDao.getMangaById(id)?.toDomain()
    }
    
    override suspend fun getMangaByFilePath(filePath: String): Manga? {
        return mangaDao.getMangaByFilePath(filePath)?.toDomain()
    }
    
    override fun searchManga(query: String): Flow<List<Manga>> {
        return mangaDao.searchManga(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getFavoriteManga(): Flow<List<Manga>> {
        return mangaDao.getFavoriteManga().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMangaByStatus(status: ReadingStatus): Flow<List<Manga>> {
        return mangaDao.getAllManga().map { entities ->
            val filtered = when (status) {
                ReadingStatus.READING -> entities.filter { it.readingProgress > 0f && !it.isCompleted }
                ReadingStatus.COMPLETED -> entities.filter { it.isCompleted }
                ReadingStatus.UNREAD -> entities.filter { it.readingProgress == 0f && !it.isCompleted }
            }
            filtered.map { it.toDomain() }
        }
    }
    
    override fun getRecentManga(limit: Int): Flow<List<Manga>> {
        return mangaDao.getRecentManga(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertOrUpdateManga(manga: Manga): Long {
        val entity = manga.toEntity()
        return mangaDao.insertOrUpdateManga(entity)
    }
    
    override suspend fun insertAllManga(mangaList: List<Manga>): List<Long> {
        val entities = mangaList.map { it.toEntity() }
        return mangaDao.insertAllManga(entities)
    }
    
    override suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: ReadingStatus) {
        val manga = mangaDao.getMangaById(mangaId) ?: return
        val pageCount = manga.pageCount
        val progress = if (pageCount > 0) currentPage.toFloat() / pageCount.toFloat() else 0f
        val isCompleted = status == ReadingStatus.COMPLETED
        mangaDao.updateReadingProgress(mangaId, currentPage, progress, isCompleted, System.currentTimeMillis())
    }
    
    override suspend fun toggleFavorite(mangaId: Long) {
        mangaDao.toggleFavorite(mangaId)
    }
    
    override suspend fun updateRating(mangaId: Long, rating: Float) {
        mangaDao.updateRating(mangaId, rating)
    }
    
    override suspend fun deleteManga(manga: Manga) {
        val entity = manga.toEntity()
        mangaDao.deleteManga(entity)
    }
    
    override suspend fun deleteAllManga(mangaList: List<Manga>) {
        val entities = mangaList.map { it.toEntity() }
        mangaDao.deleteAllManga(entities)
    }
    
    override fun getMangaCount(): Flow<Int> {
        return mangaDao.getMangaCount()
    }
    
    override fun getFavoriteCount(): Flow<Int> {
        return mangaDao.getFavoriteCount()
    }
    
    override fun getCompletedCount(): Flow<Int> {
        return mangaDao.getCompletedCount()
    }

    // TODO: Implement comic file parsing and cover extraction
    override suspend fun getCover(manga: Manga): Bitmap? = withContext(Dispatchers.IO) {
        null
    }
}

/**
 * MangaEntity 转换为 Manga 领域模型
 */
private fun MangaEntity.toDomain(): Manga {
    val status = when {
        isCompleted -> ReadingStatus.COMPLETED
        currentPage > 1 -> ReadingStatus.READING
        else -> ReadingStatus.UNREAD
    }
    return Manga(
        id = id,
        title = title,
        author = author ?: "",
        description = description ?: "",
        filePath = filePath,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        isFavorite = isFavorite,
        dateAdded = dateAdded,
        lastRead = lastRead,
        rating = rating,
        coverImagePath = coverPath, // Map entity's coverPath to domain's coverImagePath
        readingStatus = status,
        tags = emptyList() // Domain model expects tags, provide empty list
    )
}

/**
 * Manga 领域模型转换为 MangaEntity
 */
private fun Manga.toEntity(): MangaEntity {
    val progress = if (pageCount > 0) currentPage.toFloat() / pageCount.toFloat() else 0f
    return MangaEntity(
        id = id,
        title = title,
        author = author,
        description = description,
        filePath = filePath,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        isFavorite = isFavorite,
        dateAdded = dateAdded,
        lastRead = lastRead ?: 0L,
        rating = rating,
        coverPath = coverImagePath,
        readingProgress = progress,
        isCompleted = readingStatus == ReadingStatus.COMPLETED,
        format = filePath.substringAfterLast('.', ""),
        readingTime = 0, // Domain model doesn't have this, provide default
        createdAt = dateAdded, // Use dateAdded as createdAt
        updatedAt = System.currentTimeMillis()
    )
}
