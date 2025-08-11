package com.easycomic.data.repository

import com.easycomic.data.dao.ReadingHistoryDao
import com.easycomic.data.entity.ReadingHistoryEntity
import com.easycomic.domain.model.ReadingHistory
import com.easycomic.domain.repository.ReadingHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 阅读历史仓库实现类
 */
class ReadingHistoryRepositoryImpl(
    private val readingHistoryDao: ReadingHistoryDao
) : ReadingHistoryRepository {
    
    override fun getReadingHistoryByMangaId(mangaId: Long): Flow<List<ReadingHistory>> {
        return readingHistoryDao.getReadingHistoryByMangaId(mangaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentReadingHistory(limit: Int): Flow<List<ReadingHistory>> {
        return readingHistoryDao.getRecentReadingHistory(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getLatestReadingHistory(mangaId: Long): ReadingHistory? {
        return readingHistoryDao.getLatestReadingHistory(mangaId)?.toDomain()
    }
    
    override suspend fun addReadingHistory(history: ReadingHistory): Long {
        val entity = history.toEntity()
        return readingHistoryDao.insertReadingHistory(entity)
    }
    
    override suspend fun updateReadingHistory(history: ReadingHistory) {
        val entity = history.toEntity()
        readingHistoryDao.updateReadingHistory(entity)
    }
    
    override suspend fun deleteReadingHistory(history: ReadingHistory) {
        val entity = history.toEntity()
        readingHistoryDao.deleteReadingHistory(entity)
    }
    
    override suspend fun deleteReadingHistoryById(historyId: Long) {
        readingHistoryDao.deleteReadingHistoryById(historyId)
    }
    
    override suspend fun deleteReadingHistoryByMangaId(mangaId: Long) {
        readingHistoryDao.deleteReadingHistoryByMangaId(mangaId)
    }
    
    override suspend fun deleteAllReadingHistory(historyList: List<ReadingHistory>) {
        val entities = historyList.map { it.toEntity() }
        readingHistoryDao.deleteAllReadingHistory(entities)
    }
    
    override suspend fun cleanupOldReadingHistory(cutoffTime: Long) {
        readingHistoryDao.cleanupOldReadingHistory(cutoffTime)
    }
    
    override fun getReadingHistoryCount(): Flow<Int> {
        return readingHistoryDao.getReadingHistoryCount()
    }
    
    override suspend fun getReadingHistoryCountByMangaId(mangaId: Long): Int {
        return readingHistoryDao.getReadingHistoryCountByMangaId(mangaId)
    }
    
    override fun getTotalReadingDuration(): Flow<Long?> {
        return readingHistoryDao.getTotalReadingDuration()
    }
    
    override suspend fun getReadingDurationByMangaId(mangaId: Long): Long? {
        return readingHistoryDao.getReadingDurationByMangaId(mangaId)
    }
    
    override fun getDailyReadingStats(startTime: Long): Flow<List<ReadingHistoryRepository.DailyReadingStats>> {
        // 暂时返回空数据
        return kotlinx.coroutines.flow.flowOf(emptyList())
    }
}

/**
 * ReadingHistoryEntity 转换为 ReadingHistory 领域模型
 */
private fun ReadingHistoryEntity.toDomain(): ReadingHistory {
    return ReadingHistory(
        id = id,
        mangaId = mangaId,
        pageNumber = pageNumber,
        progressPercentage = progressPercentage,
        readingDuration = readingDuration,
        readAt = readAt,
        sessionId = sessionId
    )
}

/**
 * ReadingHistory 领域模型转换为 ReadingHistoryEntity
 */
private fun ReadingHistory.toEntity(): ReadingHistoryEntity {
    return ReadingHistoryEntity(
        id = id,
        mangaId = mangaId,
        pageNumber = pageNumber,
        progressPercentage = progressPercentage,
        readingDuration = readingDuration,
        readAt = readAt,
        sessionId = sessionId
    )
}