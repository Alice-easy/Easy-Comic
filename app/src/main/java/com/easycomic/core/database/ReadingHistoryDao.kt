package com.easycomic.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY lastReadTimestamp DESC")
    fun getAllReadingHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE mangaId = :mangaId")
    suspend fun getReadingHistoryForManga(mangaId: Long): ReadingHistory?

    @Query("SELECT * FROM reading_history WHERE id = :id")
    suspend fun getReadingHistoryById(id: Long): ReadingHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingHistory(history: ReadingHistory): Long

    @Update
    suspend fun updateReadingHistory(history: ReadingHistory)

    @Delete
    suspend fun deleteReadingHistory(history: ReadingHistory)

    @Query("UPDATE reading_history SET lastPageRead = :page, lastReadTimestamp = :timestamp, readingTime = readingTime + :sessionTime, readingSessions = readingSessions + 1, averagePagesPerSession = :avgPages WHERE mangaId = :mangaId")
    suspend fun updateReadingProgress(mangaId: Long, page: Int, timestamp: Long, sessionTime: Long, avgPages: Float)

    @Query("UPDATE reading_history SET isCompleted = :isCompleted WHERE mangaId = :mangaId")
    suspend fun updateCompletionStatus(mangaId: Long, isCompleted: Boolean)

    @Query("DELETE FROM reading_history WHERE mangaId = :mangaId")
    suspend fun deleteReadingHistoryForManga(mangaId: Long)

    @Query("SELECT COUNT(*) FROM reading_history WHERE isCompleted = 1")
    suspend fun getCompletedMangaCount(): Int

    @Query("SELECT COUNT(*) FROM reading_history WHERE isCompleted = 0")
    suspend fun getInProgressMangaCount(): Int

    @Query("SELECT * FROM reading_history WHERE isCompleted = 0 ORDER BY lastReadTimestamp DESC")
    fun getInProgressReadingHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT * FROM reading_history WHERE isCompleted = 1 ORDER BY lastReadTimestamp DESC")
    fun getCompletedReadingHistory(): Flow<List<ReadingHistory>>

    @Query("SELECT SUM(readingTime) FROM reading_history WHERE mangaId = :mangaId")
    suspend fun getTotalReadingTimeForManga(mangaId: Long): Long

    @Query("SELECT AVG(averagePagesPerSession) FROM reading_history")
    suspend fun getAveragePagesPerSession(): Float
}