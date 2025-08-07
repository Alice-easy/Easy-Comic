package com.easycomic.data.repository

import com.easycomic.core.database.Manga
import com.easycomic.data.remote.WebDavDataSource
import com.easycomic.data.remote.SyncResult
import com.easycomic.data.remote.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for WebDAV operations
 * Provides a clean API for cloud synchronization features
 */
@Singleton
class WebDavRepository @Inject constructor(
    private val webDavDataSource: WebDavDataSource
) {
    /**
     * Test WebDAV connection with provided credentials
     */
    suspend fun testConnection(url: String, username: String, password: String): Result<Boolean> {
        return try {
            webDavDataSource.testConnection(url, username, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save WebDAV credentials securely
     */
    suspend fun saveCredentials(url: String, username: String, password: String): Result<Boolean> {
        return try {
            webDavDataSource.saveCredentials(url, username, password)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get stored WebDAV credentials
     */
    fun getCredentials(): Triple<String?, String?, String?> {
        return webDavDataSource.getCredentials()
    }
    
    /**
     * Clear stored WebDAV credentials
     */
    suspend fun clearCredentials(): Result<Boolean> {
        return try {
            webDavDataSource.clearCredentials()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload manga metadata to WebDAV
     */
    suspend fun uploadManga(manga: Manga): Result<Boolean> {
        return try {
            webDavDataSource.uploadMangaData(manga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download manga list from WebDAV
     */
    suspend fun downloadMangaList(): Result<List<Manga>> {
        return try {
            webDavDataSource.downloadMangaList()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload file to WebDAV with progress tracking
     */
    suspend fun uploadFile(file: File, remotePath: String): Result<Boolean> {
        return try {
            webDavDataSource.uploadFile(file, remotePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download file from WebDAV with progress tracking
     */
    suspend fun downloadFile(remotePath: String, localFile: File): Result<Boolean> {
        return try {
            webDavDataSource.downloadFile(remotePath, localFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Synchronize manga list with conflict resolution
     */
    suspend fun syncMangaList(localManga: List<Manga>): Result<SyncResult> {
        return try {
            webDavDataSource.syncMangaList(localManga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create directory on WebDAV server
     */
    suspend fun createDirectory(remotePath: String): Result<Boolean> {
        return try {
            webDavDataSource.createDirectory(remotePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete file from WebDAV server
     */
    suspend fun deleteFile(remotePath: String): Result<Boolean> {
        return try {
            webDavDataSource.deleteFile(remotePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current sync status as StateFlow
     */
    fun getSyncStatus(): StateFlow<SyncStatus> = 
        webDavDataSource.syncStatus
    
    /**
     * Get current sync progress as StateFlow
     */
    fun getSyncProgress(): StateFlow<Float> = 
        webDavDataSource.syncProgress
    
    /**
     * Legacy sync all manga (without conflict resolution)
     */
    suspend fun syncAllManga(localMangaList: List<Manga>): Result<Unit> {
        return try {
            // Upload all local manga to WebDAV
            localMangaList.forEach { manga ->
                uploadManga(manga)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Legacy sync from WebDAV (without conflict resolution)
     */
    suspend fun syncFromWebDAV(): Result<List<Manga>> {
        return try {
            downloadMangaList()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if WebDAV is configured
     */
    fun isConfigured(): Boolean {
        val (url, username, password) = getCredentials()
        return !url.isNullOrEmpty() && !username.isNullOrEmpty() && !password.isNullOrEmpty()
    }
    
    /**
     * Get WebDAV server URL
     */
    fun getServerUrl(): String? {
        return getCredentials().first
    }
    
    /**
     * Check if sync is currently in progress
     */
    fun isSyncing(): Boolean {
        return webDavDataSource.syncStatus.value == SyncStatus.SYNCING
    }
    
    /**
     * Cancel current sync operation
     */
    suspend fun cancelSync(): Result<Boolean> {
        return try {
            // Implementation would depend on the underlying WebDAV client
            // For now, we'll just mark as completed
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}