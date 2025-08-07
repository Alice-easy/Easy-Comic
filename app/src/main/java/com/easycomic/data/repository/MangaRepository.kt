package com.easycomic.data.repository

import com.easycomic.core.database.Manga
import com.easycomic.data.local.MangaLocalDataSource
import com.easycomic.data.remote.WebDavDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MangaRepository @Inject constructor(
    private val localDataSource: MangaLocalDataSource,
    private val webDavDataSource: WebDavDataSource
) {
    fun getAllManga(): Flow<List<Manga>> = localDataSource.getAllManga()
    
    fun getFavoriteManga(): Flow<List<Manga>> = localDataSource.getFavoriteManga()
    
    suspend fun getMangaById(id: Long): Manga? = localDataSource.getMangaById(id)
    
    fun searchManga(query: String): Flow<List<Manga>> = localDataSource.searchManga(query)
    
    fun getRecentManga(limit: Int = 10): Flow<List<Manga>> = localDataSource.getRecentManga(limit)
    
    suspend fun insertManga(manga: Manga): Long = localDataSource.insertManga(manga)
    
    suspend fun updateManga(manga: Manga) {
        localDataSource.updateManga(manga)
        // Sync with WebDAV if enabled
        webDavDataSource.uploadMangaData(manga)
    }
    
    suspend fun deleteManga(manga: Manga) {
        localDataSource.deleteManga(manga)
    }
    
    suspend fun updateFavoriteStatus(mangaId: Long, isFavorite: Boolean) {
        localDataSource.updateFavoriteStatus(mangaId, isFavorite)
        getMangaById(mangaId)?.let { manga ->
            val updatedManga = manga.copy(isFavorite = isFavorite)
            webDavDataSource.uploadMangaData(updatedManga)
        }
    }
    
    suspend fun updateReadingProgress(mangaId: Long, page: Int, totalPages: Int) {
        val progress = if (totalPages > 0) (page.toFloat() / totalPages.toFloat()) * 100 else 0f
        val timestamp = System.currentTimeMillis()
        
        localDataSource.updateReadingProgress(mangaId, page, progress, timestamp)
        
        getMangaById(mangaId)?.let { manga ->
            val updatedManga = manga.copy(currentPage = page, progressPercentage = progress)
            webDavDataSource.uploadMangaData(updatedManga)
        }
    }
    
    suspend fun getMangaCount(): Int = localDataSource.getMangaCount()
    
    suspend fun getFavoriteCount(): Int = localDataSource.getFavoriteCount()
    
    suspend fun getMangaByFilePath(filePath: String): Manga? = localDataSource.getMangaByFilePath(filePath)
    
    suspend fun deleteMangaById(mangaId: Long) {
        localDataSource.deleteMangaById(mangaId)
    }
    
    fun getInProgressManga(): Flow<List<Manga>> = localDataSource.getInProgressManga()
    
    fun getCompletedManga(): Flow<List<Manga>> = localDataSource.getCompletedManga()
    
    // WebDAV sync operations
    suspend fun syncWithWebDAV(): Result<Unit> {
        return try {
            val remoteMangaList = webDavDataSource.downloadMangaList()
            if (remoteMangaList.isSuccess) {
                remoteMangaList.getOrNull()?.forEach { remoteManga ->
                    val localManga = localDataSource.getMangaByFilePath(remoteManga.filePath)
                    if (localManga == null) {
                        localDataSource.insertManga(remoteManga)
                    } else if (remoteManga.lastModified > localManga.lastModified) {
                        localDataSource.updateManga(remoteManga)
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun uploadMangaToWebDAV(mangaId: Long): Result<Boolean> {
        return try {
            val manga = getMangaById(mangaId)
            manga?.let {
                webDavDataSource.uploadMangaData(it)
            } ?: Result.failure(Exception("Manga not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getSyncStatus(): Flow<com.easycomic.data.remote.SyncStatus> = webDavDataSource.getSyncStatus()
}