package com.easycomic.data.local

import com.easycomic.core.database.Bookmark
import com.easycomic.core.database.BookmarkDao
import com.easycomic.core.database.Manga
import com.easycomic.core.database.MangaDao
import com.easycomic.core.database.ReadingHistory
import com.easycomic.core.database.ReadingHistoryDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaLocalDataSource @Inject constructor(
    private val mangaDao: MangaDao,
    private val bookmarkDao: BookmarkDao,
    private val readingHistoryDao: ReadingHistoryDao
) {
    // Manga operations
    fun getAllManga(): Flow<List<Manga>> = mangaDao.getAllManga()
    fun getFavoriteManga(): Flow<List<Manga>> = mangaDao.getFavoriteManga()
    suspend fun getMangaById(id: Long): Manga? = mangaDao.getMangaById(id)
    fun searchManga(query: String): Flow<List<Manga>> = mangaDao.searchManga(query)
    fun getRecentManga(limit: Int): Flow<List<Manga>> = mangaDao.getRecentManga(limit)
    
    suspend fun insertManga(manga: Manga): Long = mangaDao.insertManga(manga)
    suspend fun updateManga(manga: Manga) = mangaDao.updateManga(manga)
    suspend fun deleteManga(manga: Manga) = mangaDao.deleteManga(manga)
    
    suspend fun updateFavoriteStatus(mangaId: Long, isFavorite: Boolean) = 
        mangaDao.updateFavoriteStatus(mangaId, isFavorite)
    
    suspend fun updateReadingProgress(mangaId: Long, page: Int, progress: Float, timestamp: Long) = 
        mangaDao.updateReadingProgress(mangaId, page, progress, timestamp)
    
    suspend fun getMangaCount(): Int = mangaDao.getMangaCount()
    suspend fun getFavoriteCount(): Int = mangaDao.getFavoriteCount()
    suspend fun getMangaByFilePath(filePath: String): Manga? = mangaDao.getMangaByFilePath(filePath)
    suspend fun deleteMangaById(mangaId: Long) = mangaDao.deleteMangaById(mangaId)
    
    fun getInProgressManga(): Flow<List<Manga>> = mangaDao.getInProgressManga()
    fun getCompletedManga(): Flow<List<Manga>> = mangaDao.getCompletedManga()
    
    // Bookmark operations
    fun getBookmarksForManga(mangaId: Long): Flow<List<Bookmark>> = bookmarkDao.getBookmarksForManga(mangaId)
    suspend fun getBookmarkById(id: Long): Bookmark? = bookmarkDao.getBookmarkById(id)
    suspend fun getBookmarkByPage(mangaId: Long, page: Int): Bookmark? = bookmarkDao.getBookmarkByPage(mangaId, page)
    
    suspend fun insertBookmark(bookmark: Bookmark): Long = bookmarkDao.insertBookmark(bookmark)
    suspend fun updateBookmark(bookmark: Bookmark) = bookmarkDao.updateBookmark(bookmark)
    suspend fun deleteBookmark(bookmark: Bookmark) = bookmarkDao.deleteBookmark(bookmark)
    suspend fun deleteBookmarkById(bookmarkId: Long) = bookmarkDao.deleteBookmarkById(bookmarkId)
    suspend fun deleteBookmarksForManga(mangaId: Long) = bookmarkDao.deleteBookmarksForManga(mangaId)
    
    suspend fun getBookmarkCountForManga(mangaId: Long): Int = bookmarkDao.getBookmarkCountForManga(mangaId)
    fun getAllBookmarks(): Flow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
    fun getRecentBookmarksForManga(mangaId: Long, limit: Int): Flow<List<Bookmark>> = 
        bookmarkDao.getRecentBookmarksForManga(mangaId, limit)
    
    // Reading history operations
    fun getAllReadingHistory(): Flow<List<ReadingHistory>> = readingHistoryDao.getAllReadingHistory()
    suspend fun getReadingHistoryForManga(mangaId: Long): ReadingHistory? = 
        readingHistoryDao.getReadingHistoryForManga(mangaId)
    suspend fun getReadingHistoryById(id: Long): ReadingHistory? = readingHistoryDao.getReadingHistoryById(id)
    
    suspend fun insertReadingHistory(history: ReadingHistory): Long = readingHistoryDao.insertReadingHistory(history)
    suspend fun updateReadingHistory(history: ReadingHistory) = readingHistoryDao.updateReadingHistory(history)
    suspend fun deleteReadingHistory(history: ReadingHistory) = readingHistoryDao.deleteReadingHistory(history)
    
    suspend fun updateReadingProgress(
        mangaId: Long, 
        page: Int, 
        timestamp: Long, 
        sessionTime: Long, 
        avgPages: Float
    ) = readingHistoryDao.updateReadingProgress(mangaId, page, timestamp, sessionTime, avgPages)
    
    suspend fun updateCompletionStatus(mangaId: Long, isCompleted: Boolean) = 
        readingHistoryDao.updateCompletionStatus(mangaId, isCompleted)
    
    suspend fun deleteReadingHistoryForManga(mangaId: Long) = readingHistoryDao.deleteReadingHistoryForManga(mangaId)
    
    suspend fun getCompletedMangaCount(): Int = readingHistoryDao.getCompletedMangaCount()
    suspend fun getInProgressMangaCount(): Int = readingHistoryDao.getInProgressMangaCount()
    
    fun getInProgressReadingHistory(): Flow<List<ReadingHistory>> = readingHistoryDao.getInProgressReadingHistory()
    fun getCompletedReadingHistory(): Flow<List<ReadingHistory>> = readingHistoryDao.getCompletedReadingHistory()
    
    suspend fun getTotalReadingTimeForManga(mangaId: Long): Long = 
        readingHistoryDao.getTotalReadingTimeForManga(mangaId)
    
    suspend fun getAveragePagesPerSession(): Float = readingHistoryDao.getAveragePagesPerSession()
}