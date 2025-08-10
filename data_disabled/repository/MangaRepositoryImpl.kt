package com.easycomic.data.repository

import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
    
    override fun getMangaByStatus(status: com.easycomic.data.entity.ReadingStatus): Flow<List<Manga>> {
        return mangaDao.getMangaByStatus(status).map { entities ->
            entities.map { it.toDomain() }
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
    
    override suspend fun updateReadingProgress(
        mangaId: Long,
        currentPage: Int,
        status: com.easycomic.data.entity.ReadingStatus
    ) {
        mangaDao.updateReadingProgress(mangaId, currentPage, status)
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
}

/**
 * MangaEntity 转换为 Manga 领域模型
 */
private fun MangaEntity.toDomain(): Manga {
    return Manga(
        id = id,
        title = title,
        author = author,
        description = description,
        filePath = filePath,
        fileUri = fileUri,
        fileFormat = fileFormat,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        coverImagePath = coverImagePath,
        thumbnailPath = thumbnailPath,
        rating = rating,
        isFavorite = isFavorite,
        readingStatus = readingStatus,
        tags = if (tags.isBlank()) emptyList() else tags.split(",").map { it.trim() },
        lastRead = lastRead,
        dateAdded = dateAdded,
        dateModified = dateModified
    )
}

/**
 * Manga 领域模型转换为 MangaEntity
 */
private fun Manga.toEntity(): MangaEntity {
    return MangaEntity(
        id = id,
        title = title,
        author = author,
        description = description,
        filePath = filePath,
        fileUri = fileUri,
        fileFormat = fileFormat,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        coverImagePath = coverImagePath,
        thumbnailPath = thumbnailPath,
        rating = rating,
        isFavorite = isFavorite,
        readingStatus = readingStatus,
        tags = tags.joinToString(","),
        lastRead = lastRead,
        dateAdded = dateAdded,
        dateModified = dateModified
    )
}