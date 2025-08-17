package com.easycomic.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.easycomic.data.entity.MangaEntity
import kotlinx.coroutines.flow.Flow

/**
 * 漫画数据访问对象
 * 接口方法根据 README.md 的数据库设计进行修正
 */
@Dao
interface MangaDao {

    /**
     * 插入或更新漫画
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateManga(manga: MangaEntity): Long

    /**
     * 批量插入漫画
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllManga(mangaList: List<MangaEntity>): List<Long>

    /**
     * 获取所有漫画（按添加时间倒序）
     */
    @Query("SELECT * FROM manga ORDER BY date_added DESC")
    fun getAllManga(): Flow<List<MangaEntity>>

    /**
     * 获取漫画详情
     */
    @Query("SELECT * FROM manga WHERE id = :mangaId")
    suspend fun getMangaById(mangaId: Long): MangaEntity?

    /**
     * 根据文件路径获取漫画
     */
    @Query("SELECT * FROM manga WHERE file_path = :filePath")
    suspend fun getMangaByFilePath(filePath: String): MangaEntity?

    /**
     * 搜索漫画 (移除了 tags 字段)
     */
    @Query("""
        SELECT * FROM manga 
        WHERE title LIKE '%' || :query || '%' 
           OR author LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN title LIKE :query THEN 1
                WHEN author LIKE :query THEN 2
                ELSE 3
            END,
            title ASC
    """)
    fun searchManga(query: String): Flow<List<MangaEntity>>

    /**
     * 获取收藏的漫画
     */
    @Query("SELECT * FROM manga WHERE is_favorite = 1 ORDER BY last_read DESC")
    fun getFavoriteManga(): Flow<List<MangaEntity>>

    /**
     * 获取已完成的漫画
     */
    @Query("SELECT * FROM manga WHERE is_completed = 1 ORDER BY last_read DESC")
    fun getCompletedManga(): Flow<List<MangaEntity>>

    /**
     * 获取最近阅读的漫画
     */
    @Query("SELECT * FROM manga WHERE last_read > 0 ORDER BY last_read DESC LIMIT :limit")
    fun getRecentManga(limit: Int = 10): Flow<List<MangaEntity>>

    /**
     * 更新阅读进度
     */
    @Query("""
        UPDATE manga 
        SET current_page = :currentPage, 
            reading_progress = :readingProgress,
            is_completed = :isCompleted,
            last_read = :timestamp,
            updated_at = :timestamp
        WHERE id = :mangaId
    """)
    suspend fun updateReadingProgress(
        mangaId: Long,
        currentPage: Int,
        readingProgress: Float,
        isCompleted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    /**
     * 切换收藏状态
     */
    @Query("""
        UPDATE manga 
        SET is_favorite = NOT is_favorite, 
            updated_at = :timestamp 
        WHERE id = :mangaId
    """)
    suspend fun toggleFavorite(mangaId: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * 更新评分
     */
    @Query("""
        UPDATE manga 
        SET rating = :rating, 
            updated_at = :timestamp 
        WHERE id = :mangaId
    """)
    suspend fun updateRating(mangaId: Long, rating: Float, timestamp: Long = System.currentTimeMillis())

    /**
     * 删除漫画
     */
    @Delete
    suspend fun deleteManga(manga: MangaEntity)

    /**
     * 批量删除漫画
     */
    @Delete
    suspend fun deleteAllManga(mangaList: List<MangaEntity>)

    /**
     * 获取漫画总数
     */
    @Query("SELECT COUNT(*) FROM manga")
    fun getMangaCount(): Flow<Int>

    /**
     * 获取收藏漫画数量
     */
    @Query("SELECT COUNT(*) FROM manga WHERE is_favorite = 1")
    fun getFavoriteCount(): Flow<Int>

    /**
     * 获取已读漫画数量
     */
    @Query("SELECT COUNT(*) FROM manga WHERE is_completed = 1")
    fun getCompletedCount(): Flow<Int>
}
