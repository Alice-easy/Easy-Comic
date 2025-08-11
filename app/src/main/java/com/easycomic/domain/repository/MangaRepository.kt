package com.easycomic.domain.repository

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.Bookmark
import com.easycomic.domain.model.ReadingHistory
import com.easycomic.domain.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

/**
 * 漫画仓库接口
 */
interface MangaRepository {
    
    /**
     * 获取所有漫画
     */
    fun getAllManga(): Flow<List<Manga>>
    
    /**
     * 获取漫画详情
     */
    suspend fun getMangaById(id: Long): Manga?
    
    /**
     * 根据文件路径获取漫画
     */
    suspend fun getMangaByFilePath(filePath: String): Manga?
    
    /**
     * 搜索漫画
     */
    fun searchManga(query: String): Flow<List<Manga>>
    
    /**
     * 获取收藏的漫画
     */
    fun getFavoriteManga(): Flow<List<Manga>>
    
    /**
     * 根据阅读状态获取漫画
     */
    fun getMangaByStatus(status: ReadingStatus): Flow<List<Manga>>
    
    /**
     * 获取最近阅读的漫画
     */
    fun getRecentManga(limit: Int = 10): Flow<List<Manga>>
    
    /**
     * 添加或更新漫画
     */
    suspend fun insertOrUpdateManga(manga: Manga): Long
    
    /**
     * 批量添加漫画
     */
    suspend fun insertAllManga(mangaList: List<Manga>): List<Long>
    
    /**
     * 更新阅读进度
     */
    suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: ReadingStatus)
    
    /**
     * 切换收藏状态
     */
    suspend fun toggleFavorite(mangaId: Long)
    
    /**
     * 更新评分
     */
    suspend fun updateRating(mangaId: Long, rating: Float)
    
    /**
     * 删除漫画
     */
    suspend fun deleteManga(manga: Manga)
    
    /**
     * 批量删除漫画
     */
    suspend fun deleteAllManga(mangaList: List<Manga>)
    
    /**
     * 获取漫画总数
     */
    fun getMangaCount(): Flow<Int>
    
    /**
     * 获取收藏漫画数量
     */
    fun getFavoriteCount(): Flow<Int>
    
    /**
     * 获取已读漫画数量
     */
    fun getCompletedCount(): Flow<Int>
}