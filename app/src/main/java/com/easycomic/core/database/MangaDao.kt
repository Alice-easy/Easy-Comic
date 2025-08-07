package com.easycomic.core.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga ORDER BY dateAdded DESC")
    fun getAllManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun getFavoriteManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE id = :id")
    suspend fun getMangaById(id: Long): Manga?

    @Query("SELECT * FROM manga WHERE title LIKE :searchQuery OR author LIKE :searchQuery OR tags LIKE :searchQuery ORDER BY title ASC")
    fun searchManga(searchQuery: String): Flow<List<Manga>>

    @Query("SELECT * FROM manga ORDER BY lastModified DESC LIMIT :limit")
    fun getRecentManga(limit: Int): Flow<List<Manga>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManga(manga: Manga): Long

    @Update
    suspend fun updateManga(manga: Manga)

    @Delete
    suspend fun deleteManga(manga: Manga)

    @Query("UPDATE manga SET isFavorite = :isFavorite WHERE id = :mangaId")
    suspend fun updateFavoriteStatus(mangaId: Long, isFavorite: Boolean)

    @Query("UPDATE manga SET currentPage = :page, progressPercentage = :progress, lastModified = :timestamp WHERE id = :mangaId")
    suspend fun updateReadingProgress(mangaId: Long, page: Int, progress: Float, timestamp: Long)

    @Query("SELECT COUNT(*) FROM manga")
    suspend fun getMangaCount(): Int

    @Query("SELECT COUNT(*) FROM manga WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT * FROM manga WHERE filePath = :filePath")
    suspend fun getMangaByFilePath(filePath: String): Manga?

    @Query("DELETE FROM manga WHERE id = :mangaId")
    suspend fun deleteMangaById(mangaId: Long)

    @Query("SELECT * FROM manga WHERE progressPercentage > 0 AND progressPercentage < 100 ORDER BY lastModified DESC")
    fun getInProgressManga(): Flow<List<Manga>>

    @Query("SELECT * FROM manga WHERE progressPercentage = 100 ORDER BY lastModified DESC")
    fun getCompletedManga(): Flow<List<Manga>>
}