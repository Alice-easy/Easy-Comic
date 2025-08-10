package com.easycomic.domain.repository

import com.easycomic.domain.model.ReadingHistory
import kotlinx.coroutines.flow.Flow

/**
 * 阅读历史仓库接口
 */
interface ReadingHistoryRepository {
    
    /**
     * 获取指定漫画的阅读历史
     */
    fun getReadingHistoryByMangaId(mangaId: Long): Flow<List<ReadingHistory>>
    
    /**
     * 获取最近的阅读记录
     */
    fun getRecentReadingHistory(limit: Int = 50): Flow<List<ReadingHistory>>
    
    /**
     * 获取指定漫画的最新阅读记录
     */
    suspend fun getLatestReadingHistory(mangaId: Long): ReadingHistory?
    
    /**
     * 添加阅读记录
     */
    suspend fun addReadingHistory(history: ReadingHistory): Long
    
    /**
     * 更新阅读记录
     */
    suspend fun updateReadingHistory(history: ReadingHistory)
    
    /**
     * 删除阅读记录
     */
    suspend fun deleteReadingHistory(history: ReadingHistory)
    
    /**
     * 根据ID删除阅读记录
     */
    suspend fun deleteReadingHistoryById(historyId: Long)
    
    /**
     * 删除指定漫画的所有阅读记录
     */
    suspend fun deleteReadingHistoryByMangaId(mangaId: Long)
    
    /**
     * 批量删除阅读记录
     */
    suspend fun deleteAllReadingHistory(historyList: List<ReadingHistory>)
    
    /**
     * 清理旧的阅读记录
     */
    suspend fun cleanupOldReadingHistory(cutoffTime: Long)
    
    /**
     * 获取阅读记录总数
     */
    fun getReadingHistoryCount(): Flow<Int>
    
    /**
     * 获取指定漫画的阅读记录数量
     */
    suspend fun getReadingHistoryCountByMangaId(mangaId: Long): Int
    
    /**
     * 获取总阅读时长
     */
    fun getTotalReadingDuration(): Flow<Long?>
    
    /**
     * 获取指定漫画的总阅读时长
     */
    suspend fun getReadingDurationByMangaId(mangaId: Long): Long?
    
    /**
     * 获取每日阅读统计
     */
    fun getDailyReadingStats(startTime: Long): Flow<List<DailyReadingStats>>
    
    /**
     * 每日阅读统计数据
     */
    data class DailyReadingStats(
        val date: String,
        val sessionCount: Int,
        val totalDuration: Long
    )
}