package com.easycomic.data.repository

import com.easycomic.core.database.Bookmark
import com.easycomic.core.database.BookmarkDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getBookmarksForManga(mangaId: Long): Flow<List<Bookmark>> = 
        bookmarkDao.getBookmarksForManga(mangaId)
    
    suspend fun getBookmarkById(id: Long): Bookmark? = bookmarkDao.getBookmarkById(id)
    
    suspend fun getBookmarkByPage(mangaId: Long, page: Int): Bookmark? = 
        bookmarkDao.getBookmarkByPage(mangaId, page)
    
    suspend fun addBookmark(bookmark: Bookmark): Long = bookmarkDao.insertBookmark(bookmark)
    
    suspend fun updateBookmark(bookmark: Bookmark) = bookmarkDao.updateBookmark(bookmark)
    
    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)
    
    suspend fun deleteBookmarkById(bookmarkId: Long) = bookmarkDao.deleteBookmarkById(bookmarkId)
    
    suspend fun deleteBookmarksForManga(mangaId: Long) = bookmarkDao.deleteBookmarksForManga(mangaId)
    
    suspend fun getBookmarkCountForManga(mangaId: Long): Int = bookmarkDao.getBookmarkCountForManga(mangaId)
    
    fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
    
    fun getRecentBookmarksForManga(mangaId: Long, limit: Int = 5): Flow<List<Bookmark>> = 
        bookmarkDao.getRecentBookmarksForManga(mangaId, limit)
    
    suspend fun createBookmark(mangaId: Long, page: Int, name: String, note: String? = null): Long {
        val bookmark = Bookmark(
            mangaId = mangaId,
            page = page,
            name = name,
            note = note,
            timestamp = System.currentTimeMillis()
        )
        return addBookmark(bookmark)
    }
}