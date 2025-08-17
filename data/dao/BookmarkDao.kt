package com.easycomic.data.dao

import androidx.room.*
import com.easycomic.data.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

/**
 * 书签数据访问对象
 */
@Dao
interface BookmarkDao {
    
    /**
     * 插入书签
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long
    
    /**
     * 批量插入书签
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBookmarks(bookmarkList: List<BookmarkEntity>): List<Long>
    
    /**
     * 获取指定漫画的所有书签
     */
    @Query("""
        SELECT * FROM bookmark 
        WHERE manga_id = :mangaId 
        ORDER BY page_number ASC, created_at ASC
    """)
    fun getBookmarksByMangaId(mangaId: Long): Flow<List<BookmarkEntity>>
    
    /**
     * 获取指定页码的书签
     */
    @Query("""
        SELECT * FROM bookmark 
        WHERE manga_id = :mangaId AND page_number = :pageNumber
        ORDER BY created_at DESC
    """)
    suspend fun getBookmarkByPage(mangaId: Long, pageNumber: Int): List<BookmarkEntity>
    
    /**
     * 获取书签详情
     */
    @Query("SELECT * FROM bookmark WHERE id = :bookmarkId")
    suspend fun getBookmarkById(bookmarkId: Long): BookmarkEntity?
    
    /**
     * 检查指定页码是否有书签
     */
    @Query("""
        SELECT COUNT(*) FROM bookmark 
        WHERE manga_id = :mangaId AND page_number = :pageNumber
    """)
    suspend fun hasBookmarkForPage(mangaId: Long, pageNumber: Int): Int
    
    /**
     * 更新书签
     */
    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)
    
    /**
     * 删除书签
     */
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    /**
     * 根据ID删除书签
     */
    @Query("DELETE FROM bookmark WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)
    
    /**
     * 删除指定漫画的所有书签
     */
    @Query("DELETE FROM bookmark WHERE manga_id = :mangaId")
    suspend fun deleteBookmarksByMangaId(mangaId: Long)
    
    /**
     * 删除指定页码的书签
     */
    @Query("DELETE FROM bookmark WHERE manga_id = :mangaId AND page_number = :pageNumber")
    suspend fun deleteBookmarkByPage(mangaId: Long, pageNumber: Int)
    
    /**
     * 批量删除书签
     */
    @Delete
    suspend fun deleteAllBookmarks(bookmarkList: List<BookmarkEntity>)
    
    /**
     * 获取书签总数
     */
    @Query("SELECT COUNT(*) FROM bookmark")
    fun getBookmarkCount(): Flow<Int>
    
    /**
     * 获取指定漫画的书签数量
     */
    @Query("SELECT COUNT(*) FROM bookmark WHERE manga_id = :mangaId")
    suspend fun getBookmarkCountByMangaId(mangaId: Long): Int
    
    /**
     * 获取最近的添加的书签
     */
    @Query("""
        SELECT * FROM bookmark 
        ORDER BY id DESC 
        LIMIT :limit
    """)
    fun getRecentBookmarks(limit: Int = 20): Flow<List<BookmarkEntity>>
}
