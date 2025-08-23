package com.easycomic.domain.usecase

import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow

/**
 * 漫画相关用例聚合类
 * 
 * 将原来分散的13个UseCase合并为一个聚合类，减少类的数量并提高可维护性。
 * 提供漫画数据的完整业务逻辑操作，包括查询、更新、批量操作和统计功能。
 * 
 * ## 功能分类：
 * - **查询操作**：获取漫画列表、搜索、收藏等
 * - **更新操作**：修改漫画信息、阅读进度、评分等
 * - **批量操作**：批量导入、删除、标记已读等
 * - **统计操作**：获取各种统计数据
 * 
 * @property mangaRepository 漫画数据仓库，用于数据持久化操作
 * 
 * @author EasyComic Team
 * @since 1.0.0
 */
class MangaUseCases(
    private val mangaRepository: MangaRepository
) {
    
    // ========== 查询操作 ==========
    
    /**
     * 获取所有漫画列表
     * 
     * @return Flow<List<Manga>> 漫画列表的数据流
     */
    fun getAllManga(): Flow<List<Manga>> = mangaRepository.getAllManga()
    
    /**
     * 根据ID获取指定漫画
     * 
     * @param id 漫画ID
     * @return Manga? 漫画对象，如果不存在则返回null
     */
    suspend fun getMangaById(id: Long): Manga? = mangaRepository.getMangaById(id)
    
    /**
     * 搜索漫画
     * 
     * @param query 搜索关键词
     * @return Flow<List<Manga>> 搜索结果的数据流
     */
    fun searchManga(query: String): Flow<List<Manga>> = mangaRepository.searchManga(query)
    
    /**
     * 获取收藏的漫画列表
     * 
     * @return Flow<List<Manga>> 收藏漫画列表的数据流
     */
    fun getFavoriteManga(): Flow<List<Manga>> = mangaRepository.getFavoriteManga()
    
    /**
     * 获取最近阅读的漫画列表
     * 
     * @param limit 返回数量限制，默认为10
     * @return Flow<List<Manga>> 最近阅读漫画列表的数据流
     */
    fun getRecentManga(limit: Int = 10): Flow<List<Manga>> = mangaRepository.getRecentManga(limit)
    
    /**
     * 获取漫画封面
     * 
     * @param manga 漫画对象
     * @return 封面数据
     */
    suspend fun getCover(manga: Manga) = mangaRepository.getCover(manga)
    
    // ========== 更新操作 ==========
    
    suspend fun insertOrUpdateManga(manga: Manga): Long = mangaRepository.insertOrUpdateManga(manga)
    
    suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: ReadingStatus) {
        mangaRepository.updateReadingProgress(mangaId, currentPage, status)
    }
    
    suspend fun toggleFavorite(mangaId: Long) = mangaRepository.toggleFavorite(mangaId)
    
    suspend fun updateRating(mangaId: Long, rating: Float) = mangaRepository.updateRating(mangaId, rating)
    
    // ========== 批量操作 ==========
    
    suspend fun insertAllManga(mangaList: List<Manga>): List<Long> = mangaRepository.insertAllManga(mangaList)
    
    suspend fun deleteManga(manga: Manga) = mangaRepository.deleteManga(manga)
    
    suspend fun deleteAllManga(mangaList: List<Manga>) = mangaRepository.deleteAllManga(mangaList)
    
    suspend fun markMangasAsRead(mangaIds: List<Long>) {
        mangaIds.forEach { mangaId ->
            val manga = getMangaById(mangaId)
            manga?.let {
                updateReadingProgress(mangaId, it.pageCount, ReadingStatus.COMPLETED)
            }
        }
    }
    
    // ========== 统计操作 ==========
    
    fun getMangaCount(): Flow<Int> = mangaRepository.getMangaCount()
    
    fun getFavoriteCount(): Flow<Int> = mangaRepository.getFavoriteCount()
    
    fun getCompletedCount(): Flow<Int> = mangaRepository.getCompletedCount()
}