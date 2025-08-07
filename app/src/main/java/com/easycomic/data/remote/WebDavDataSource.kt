package com.easycomic.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.easycomic.core.database.Manga
import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import com.github.sardine.DavResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * WebDAV data source for cloud synchronization
 * Handles file upload/download, authentication, and conflict resolution
 */
@Singleton
class WebDavDataSource @Inject constructor(
    private val context: Context
) {
    private val sardine: Sardine = SardineFactory.begin()
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.IDLE)
    private val _syncProgress = MutableStateFlow<Float>(0f)
    private val isSyncing = AtomicBoolean(false)
    
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()
    
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "webdav_credentials",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Test WebDAV connection with provided credentials
     */
    suspend fun testConnection(url: String, username: String, password: String): Result<Boolean> = suspendCancellableCoroutine { continuation ->
        try {
            val normalizedUrl = normalizeUrl(url)
            sardine.setCredentials(username, password)
            
            sardine.exists(normalizedUrl) { success, response ->
                if (success) {
                    continuation.resume(Result.success(true))
                } else {
                    continuation.resume(Result.failure(IOException("Connection failed: ${response.statusCode}")))
                }
            }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }
    
    /**
     * Save WebDAV credentials securely
     */
    suspend fun saveCredentials(url: String, username: String, password: String): Result<Boolean> {
        return try {
            encryptedPrefs.edit()
                .putString("webdav_url", url)
                .putString("webdav_username", username)
                .putString("webdav_password", password)
                .apply()
            Result.success(true)
        } catch (e: GeneralSecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get stored WebDAV credentials
     */
    fun getCredentials(): Triple<String?, String?, String?> {
        return Triple(
            encryptedPrefs.getString("webdav_url", null),
            encryptedPrefs.getString("webdav_username", null),
            encryptedPrefs.getString("webdav_password", null)
        )
    }
    
    /**
     * Clear stored credentials
     */
    suspend fun clearCredentials(): Result<Boolean> {
        return try {
            encryptedPrefs.edit()
                .remove("webdav_url")
                .remove("webdav_username")
                .remove("webdav_password")
                .apply()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Upload manga metadata to WebDAV
     */
    suspend fun uploadMangaData(manga: Manga): Result<Boolean> {
        if (!isSyncing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Sync already in progress"))
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            
            // Convert manga to JSON and upload
            val mangaJson = mangaToJson(manga)
            val remotePath = "${normalizeUrl(url)}/manga/${manga.id}.json"
            
            uploadString(mangaJson, remotePath)
            _syncProgress.value = 1f
            _syncStatus.value = SyncStatus.SUCCESS
            
            Result.success(true)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * Download manga list from WebDAV
     */
    suspend fun downloadMangaList(): Result<List<Manga>> {
        if (!isSyncing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Sync already in progress"))
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            val mangaDir = "${normalizeUrl(url)}/manga"
            
            if (!sardine.exists(mangaDir)) {
                return Result.success(emptyList())
            }
            
            val resources = sardine.list(mangaDir)
            val mangaList = mutableListOf<Manga>()
            
            resources.forEachIndexed { index, resource ->
                if (resource.path.endsWith(".json") && !resource.path.endsWith("/")) {
                    try {
                        val jsonContent = downloadString(resource.path)
                        val manga = jsonToManga(jsonContent)
                        mangaList.add(manga)
                    } catch (e: Exception) {
                        // Log error but continue with other files
                        e.printStackTrace()
                    }
                }
                _syncProgress.value = (index + 1f) / resources.size
            }
            
            _syncStatus.value = SyncStatus.SUCCESS
            Result.success(mangaList)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * Upload file to WebDAV with progress tracking
     */
    suspend fun uploadFile(file: File, remotePath: String): Result<Boolean> {
        if (!isSyncing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Sync already in progress"))
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            val fullRemotePath = "${normalizeUrl(url)}/$remotePath"
            
            file.inputStream().use { inputStream ->
                uploadStream(inputStream, fullRemotePath, file.length())
            }
            
            _syncProgress.value = 1f
            _syncStatus.value = SyncStatus.SUCCESS
            
            Result.success(true)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * Download file from WebDAV with progress tracking
     */
    suspend fun downloadFile(remotePath: String, localFile: File): Result<Boolean> {
        if (!isSyncing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Sync already in progress"))
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            val fullRemotePath = "${normalizeUrl(url)}/$remotePath"
            
            localFile.outputStream().use { outputStream ->
                downloadStream(fullRemotePath, outputStream)
            }
            
            _syncProgress.value = 1f
            _syncStatus.value = SyncStatus.SUCCESS
            
            Result.success(true)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * Synchronize manga list with conflict resolution
     */
    suspend fun syncMangaList(localManga: List<Manga>): Result<SyncResult> {
        if (!isSyncing.compareAndSet(false, true)) {
            return Result.failure(IllegalStateException("Sync already in progress"))
        }
        
        return try {
            _syncStatus.value = SyncStatus.SYNCING
            _syncProgress.value = 0f
            
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            
            // Download remote manga list
            val remoteMangaList = downloadMangaList().getOrDefault(emptyList())
            val mergedList = mutableListOf<Manga>()
            val conflicts = mutableListOf<ConflictResolution>()
            
            // Simple conflict resolution: newest version wins
            val localMap = localManga.associateBy { it.id }
            val remoteMap = remoteMangaList.associateBy { it.id }
            
            val allIds = (localMap.keys + remoteMap.keys).toSet()
            
            allIds.forEachIndexed { index, id ->
                val local = localMap[id]
                val remote = remoteMap[id]
                
                when {
                    local == null -> {
                        // Only exists remotely
                        mergedList.add(remote)
                    }
                    remote == null -> {
                        // Only exists locally
                        mergedList.add(local)
                    }
                    else -> {
                        // Conflict resolution: newest by lastModified timestamp
                        val merged = if (local.lastModified >= remote.lastModified) {
                            local
                        } else {
                            remote
                        }
                        mergedList.add(merged)
                        
                        if (local.lastModified != remote.lastModified) {
                            conflicts.add(
                                ConflictResolution(
                                    mangaId = id,
                                    conflictType = if (local.lastModified >= remote.lastModified) "LOCAL_WINS" else "REMOTE_WINS",
                                    resolution = "NEWEST_TIMESTAMP"
                                )
                            )
                        }
                    }
                }
                
                _syncProgress.value = (index + 1f) / allIds.size
            }
            
            // Upload merged list back to server
            mergedList.forEachIndexed { index, manga ->
                uploadMangaData(manga)
                _syncProgress.value = 0.5f + (index + 1f) / (mergedList.size * 2)
            }
            
            _syncStatus.value = SyncStatus.SUCCESS
            Result.success(
                SyncResult(
                    mergedMangaList = mergedList,
                    conflicts = conflicts,
                    success = true
                )
            )
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.ERROR
            Result.failure(e)
        } finally {
            isSyncing.set(false)
        }
    }
    
    /**
     * Create directory on WebDAV server
     */
    suspend fun createDirectory(remotePath: String): Result<Boolean> {
        return try {
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            val fullRemotePath = "${normalizeUrl(url)}/$remotePath"
            
            sardine.createDirectory(fullRemotePath)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete file from WebDAV server
     */
    suspend fun deleteFile(remotePath: String): Result<Boolean> {
        return try {
            val (url, username, password) = getCredentials()
            if (url == null || username == null || password == null) {
                return Result.failure(IllegalStateException("No WebDAV credentials found"))
            }
            
            sardine.setCredentials(username, password)
            val fullRemotePath = "${normalizeUrl(url)}/$remotePath"
            
            sardine.delete(fullRemotePath)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper methods
    
    private suspend fun uploadString(content: String, remotePath: String) {
        sardine.put(remotePath, content.toByteArray())
    }
    
    private suspend fun downloadString(remotePath: String): String {
        return sardine.get(remotePath).bufferedReader().use { it.readText() }
    }
    
    private suspend fun uploadStream(inputStream: InputStream, remotePath: String, totalBytes: Long) {
        val buffer = ByteArray(8192)
        var bytesUploaded = 0L
        
        inputStream.use { input ->
            sardine.put(remotePath, input) { written ->
                bytesUploaded += written
                _syncProgress.value = bytesUploaded.toFloat() / totalBytes
            }
        }
    }
    
    private suspend fun downloadStream(remotePath: String, outputStream: OutputStream) {
        val remoteFile = sardine.get(remotePath)
        val totalBytes = remoteFile.available().toLong()
        var bytesDownloaded = 0L
        
        remoteFile.use { input ->
            outputStream.use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    bytesDownloaded += bytesRead
                    _syncProgress.value = bytesDownloaded.toFloat() / totalBytes
                }
            }
        }
    }
    
    private fun normalizeUrl(url: String): String {
        return url.removeSuffix("/")
    }
    
    private fun mangaToJson(manga: Manga): String {
        return """
            {
                "id": ${manga.id},
                "title": "${manga.title}",
                "filePath": "${manga.filePath}",
                "coverImagePath": "${manga.coverImagePath ?: ""}",
                "totalPages": ${manga.totalPages},
                "currentPage": ${manga.currentPage},
                "isFavorite": ${manga.isFavorite},
                "fileSize": ${manga.fileSize},
                "lastModified": ${manga.lastModified},
                "dateAdded": ${manga.dateAdded},
                "format": "${manga.format}",
                "author": "${manga.author ?: ""}",
                "description": "${manga.description ?: ""}",
                "tags": ${manga.tags.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }},
                "rating": ${manga.rating},
                "progressPercentage": ${manga.progressPercentage},
                "lastReadTimestamp": ${manga.lastReadTimestamp}
            }
        """.trimIndent().replace("\n", "")
    }
    
    private fun jsonToManga(json: String): Manga {
        // Simplified JSON parsing - in production, use proper JSON library
        // This is a basic implementation for demonstration
        return Manga(
            id = 1, // Parse from JSON
            title = "", // Parse from JSON
            filePath = "" // Parse from JSON
        )
    }
}

/**
 * Sync operation status
 */
enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

/**
 * Result of synchronization operation
 */
data class SyncResult(
    val mergedMangaList: List<Manga>,
    val conflicts: List<ConflictResolution>,
    val success: Boolean
)

/**
 * Conflict resolution information
 */
data class ConflictResolution(
    val mangaId: Long,
    val conflictType: String, // LOCAL_WINS, REMOTE_WINS, MANUAL_RESOLUTION
    val resolution: String
)