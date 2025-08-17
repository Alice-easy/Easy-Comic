package com.easycomic.domain.repository

import com.easycomic.domain.model.Bookmark
import kotlinx.coroutines.flow.Flow

/**
 * 书签仓库接口
 */
interface BookmarkRepository {
    
    /**
     * 获取指定漫画的所有书签
     */
    fun getBookmarksByMangaId(mangaId: Long): Flow<List<Bookmark>>
    
    /**
     * 获取指定页码的书签
     */
    suspend fun getBookmarkByPage(mangaId: Long, pageNumber: Int): List<Bookmark>
    
    /**
     * 获取书签详情
     */
    suspend fun getBookmarkById(bookmarkId: Long): Bookmark?
    
    /**
     * 检查指定页码是否有书签
     */
    suspend fun hasBookmarkForPage(mangaId: Long, pageNumber: Int): Boolean
    
    /**
     * 添加书签
     */
    suspend fun addBookmark(bookmark: Bookmark): Long
    
    /**
     * 更新书签
     */
    suspend fun updateBookmark(bookmark: Bookmark)
    
    /**
     * 删除书签
     */
    suspend fun deleteBookmark(bookmark: Bookmark)
    
    /**
     * 根据ID删除书签
     */
    suspend fun deleteBookmarkById(bookmarkId: Long)
    
    /**
     * 删除指定漫画的所有书签
     */
    suspend fun deleteBookmarksByMangaId(mangaId: Long)
    
    /**
     * 删除指定页码的书签
     */
    suspend fun deleteBookmarkByPage(mangaId: Long, pageNumber: Int)
    
    /**
     * 批量删除书签
     */
    suspend fun deleteAllBookmarks(bookmarkList: List<Bookmark>)
    
    /**
     * 获取书签总数
     */
    fun getBookmarkCount(): Flow<Int>
    
    /**
     * 获取指定漫画的书签数量
     */
    suspend fun getBookmarkCountByMangaId(mangaId: Long): Int
    
    /**
     * 获取最近添加的书签
     */
    fun getRecentBookmarks(limit: Int = 20): Flow<List<Bookmark>>
}