package com.easycomic.data.repository

import com.easycomic.core.database.ReadingHistory
import com.easycomic.core.database.ReadingHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingHistoryRepository @Inject constructor(
    private val readingHistoryDao: ReadingHistoryDao
) {
    fun getAllReadingHistory(): Flow<List<ReadingHistory>> = readingHistoryDao.getAllReadingHistory()
    
    suspend fun getReadingHistoryForManga(mangaId: Long): ReadingHistory? = 
        readingHistoryDao.getReadingHistoryForManga(mangaId)
    
    suspend fun getReadingHistoryById(id: Long): ReadingHistory? = readingHistoryDao.getReadingHistoryById(id)
    
    suspend fun addReadingHistory(history: ReadingHistory): Long = readingHistoryDao.insertReadingHistory(history)
    
    suspend fun updateReadingHistory(history: ReadingHistory) = readingHistoryDao.updateReadingHistory(history)
    
    suspend fun deleteReadingHistory(history: ReadingHistory) = readingHistoryDao.deleteReadingHistory(history)
    
    suspend fun updateReadingProgress(
        mangaId: Long, 
        currentPage: Int, 
        totalPages: Int, 
        sessionTime: Long
    ) {
        val timestamp = System.currentTimeMillis()
        val avgPages = if (sessionTime > 0) currentPage.toFloat() / (sessionTime / 1000f / 60f) else 0f
        
        readingHistoryDao.updateReadingProgress(mangaId, currentPage, timestamp, sessionTime, avgPages)
    }
    
    suspend fun markAsCompleted(mangaId: Long, isCompleted: Boolean) = 
        readingHistoryDao.updateCompletionStatus(mangaId, isCompleted)
    
    suspend fun deleteReadingHistoryForManga(mangaId: Long) = 
        readingHistoryDao.deleteReadingHistoryForManga(mangaId)
    
    suspend fun getCompletedMangaCount(): Int = readingHistoryDao.getCompletedMangaCount()
    
    suspend fun getInProgressMangaCount(): Int = readingHistoryDao.getInProgressMangaCount()
    
    fun getInProgressReadingHistory(): Flow<List<ReadingHistory>> = 
        readingHistoryDao.getInProgressReadingHistory()
    
    fun getCompletedReadingHistory(): Flow<List<ReadingHistory>> = 
        readingHistoryDao.getCompletedReadingHistory()
    
    suspend fun getTotalReadingTimeForManga(mangaId: Long): Long = 
        readingHistoryDao.getTotalReadingTimeForManga(mangaId)
    
    suspend fun getAveragePagesPerSession(): Float = readingHistoryDao.getAveragePagesPerSession()
    
    suspend fun startReadingSession(mangaId: Long, totalPages: Int): Long {
        val existingHistory = getReadingHistoryForManga(mangaId)
        return if (existingHistory != null) {
            existingHistory.id
        } else {
            val history = ReadingHistory(
                mangaId = mangaId,
                totalPages = totalPages,
                lastPageRead = 0,
                readingTime = 0,
                lastReadTimestamp = System.currentTimeMillis(),
                isCompleted = false,
                readingSessions = 0,
                averagePagesPerSession = 0f
            )
            addReadingHistory(history)
        }
    }
}