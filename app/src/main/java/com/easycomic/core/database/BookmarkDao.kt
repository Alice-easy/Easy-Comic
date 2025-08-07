package com.easycomic.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark WHERE mangaId = :mangaId ORDER BY page ASC")
    fun getBookmarksForManga(mangaId: Long): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE id = :id")
    suspend fun getBookmarkById(id: Long): Bookmark?

    @Query("SELECT * FROM bookmark WHERE mangaId = :mangaId AND page = :page")
    suspend fun getBookmarkByPage(mangaId: Long, page: Int): Bookmark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: Bookmark): Long

    @Update
    suspend fun updateBookmark(bookmark: Bookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: Bookmark)

    @Query("DELETE FROM bookmark WHERE id = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: Long)

    @Query("DELETE FROM bookmark WHERE mangaId = :mangaId")
    suspend fun deleteBookmarksForManga(mangaId: Long)

    @Query("SELECT COUNT(*) FROM bookmark WHERE mangaId = :mangaId")
    suspend fun getBookmarkCountForManga(mangaId: Long): Int

    @Query("SELECT * FROM bookmark ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmark WHERE mangaId = :mangaId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBookmarksForManga(mangaId: Long, limit: Int): Flow<List<Bookmark>>
}