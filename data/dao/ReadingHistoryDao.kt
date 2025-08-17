package com.easycomic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easycomic.data.entity.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 阅读历史数据访问对象
 * 接口方法根据 README.md 的数据库设计进行修正
 */
@Dao
interface ReadingHistoryDao {

    /**
     * 插入阅读记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingHistory(history: ReadingHistoryEntity): Long

    /**
     * 批量插入阅读记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReadingHistory(historyList: List<ReadingHistoryEntity>): List<Long>

    /**
     * 获取指定漫画的阅读历史
     */
    @Query("SELECT * FROM reading_history WHERE manga_id = :mangaId ORDER BY session_end DESC")
    fun getReadingHistoryByMangaId(mangaId: Long): Flow<List<ReadingHistoryEntity>>

    /**
     * 获取最近的阅读记录
     */
    @Query("SELECT * FROM reading_history ORDER BY session_end DESC LIMIT :limit")
    fun getRecentReadingHistory(limit: Int = 50): Flow<List<ReadingHistoryEntity>>

    /**
     * 获取指定漫画的最新阅读记录
     */
    @Query("SELECT * FROM reading_history WHERE manga_id = :mangaId ORDER BY session_end DESC LIMIT 1")
    suspend fun getLatestReadingHistory(mangaId: Long): ReadingHistoryEntity?

    /**
     * 更新阅读记录
     */
    @Update
    suspend fun updateReadingHistory(history: ReadingHistoryEntity)

    /**
     * 删除阅读记录
     */
    @Delete
    suspend fun deleteReadingHistory(history: ReadingHistoryEntity)

    /**
     * 根据ID删除阅读记录
     */
    @Query("DELETE FROM reading_history WHERE id = :historyId")
    suspend fun deleteReadingHistoryById(historyId: Long)

    /**
     * 删除指定漫画的所有阅读记录
     */
    @Query("DELETE FROM reading_history WHERE manga_id = :mangaId")
    suspend fun deleteReadingHistoryByMangaId(mangaId: Long)

    /**
     * 批量删除阅读记录
     */
    @Delete
    suspend fun deleteAllReadingHistory(historyList: List<ReadingHistoryEntity>)

    /**
     * 清理旧的阅读记录
     */
    @Query("DELETE FROM reading_history WHERE session_end < :cutoffTime")
    suspend fun cleanupOldReadingHistory(cutoffTime: Long)

    /**
     * 获取阅读记录总数
     */
    @Query("SELECT COUNT(*) FROM reading_history")
    fun getReadingHistoryCount(): Flow<Int>

    /**
     * 获取指定漫画的阅读记录数量
     */
    @Query("SELECT COUNT(*) FROM reading_history WHERE manga_id = :mangaId")
    suspend fun getReadingHistoryCountByMangaId(mangaId: Long): Int

    /**
     * 获取总阅读时长
     */
    @Query("SELECT SUM(reading_time) FROM reading_history")
    fun getTotalReadingDuration(): Flow<Long?>

    /**
     * 获取指定漫画的总阅读时长
     */
    @Query("SELECT SUM(reading_time) FROM reading_history WHERE manga_id = :mangaId")
    suspend fun getReadingDurationByMangaId(mangaId: Long): Long?
}
