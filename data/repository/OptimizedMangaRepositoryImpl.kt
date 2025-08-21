package com.easycomic.data.repository

import android.graphics.Bitmap
import com.easycomic.data.dao.MangaDao
import com.easycomic.data.entity.MangaEntity
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 优化后的漫画仓库实现类
 * 
 * 优化点：
 * 1. 添加内存缓存减少数据库查询
 * 2. 优化批量操作性能
 * 3. 改进数据转换逻辑
 * 4. 添加查询结果缓存
 */
class OptimizedMangaRepositoryImpl(
    private val mangaDao: MangaDao
) : MangaRepository {
    
    // 内存缓存：漫画详情缓存
    private val mangaCache = ConcurrentHashMap<Long, Manga>()
    
    // 内存缓存：文件路径到漫画的映射
    private val filePathCache = ConcurrentHashMap<String, Manga>()
    
    // 缓存过期时间（5分钟）
    private val cacheExpirationTime = 5 * 60 * 1000L
    private val cacheTimestamps = ConcurrentHashMap<Long, Long>()
    
    override fun getAllManga(): Flow<List<Manga>> {
        return mangaDao.getAllManga().map { entities ->
            entities.map { entity ->
                // 更新缓存
                val manga = entity.toDomain()
                updateCache(manga)
                manga
            }
        }
    }
    
    override suspend fun getMangaById(id: Long): Manga? = withContext(Dispatchers.IO) {
        // 先检查缓存
        val cachedManga = getCachedManga(id)
        if (cachedManga != null) {
            return@withContext cachedManga
        }
        
        // 缓存未命中，查询数据库
        val entity = mangaDao.getMangaById(id)
        val manga = entity?.toDomain()
        
        // 更新缓存
        manga?.let { updateCache(it) }
        
        manga
    }
    
    override suspend fun getMangaByFilePath(filePath: String): Manga? = withContext(Dispatchers.IO) {
        // 先检查文件路径缓存
        val cachedManga = filePathCache[filePath]
        if (cachedManga != null && !isCacheExpired(cachedManga.id)) {
            return@withContext cachedManga
        }
        
        // 缓存未命中，查询数据库
        val entity = mangaDao.getMangaByFilePath(filePath)
        val manga = entity?.toDomain()
        
        // 更新缓存
        manga?.let { 
            updateCache(it)
            filePathCache[filePath] = it
        }
        
        manga
    }
    
    override fun searchManga(query: String): Flow<List<Manga>> {
        return mangaDao.searchManga(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getFavoriteManga(): Flow<List<Manga>> {
        return mangaDao.getFavoriteManga().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMangaByStatus(status: ReadingStatus): Flow<List<Manga>> {
        // 优化：直接在数据库层过滤，而不是在内存中过滤
        return when (status) {
            ReadingStatus.READING -> mangaDao.getReadingManga()
            ReadingStatus.COMPLETED -> mangaDao.getCompletedManga()
            ReadingStatus.UNREAD -> mangaDao.getUnreadManga()
        }.map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentManga(limit: Int): Flow<List<Manga>> {
        return mangaDao.getRecentManga(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertOrUpdateManga(manga: Manga): Long = withContext(Dispatchers.IO) {
        val entity = manga.toEntity()
        val id = mangaDao.insertOrUpdateManga(entity)
        
        // 更新缓存
        val updatedManga = manga.copy(id = if (manga.id == 0L) id else manga.id)
        updateCache(updatedManga)
        filePathCache[manga.filePath] = updatedManga
        
        id
    }
    
    override suspend fun updateManga(manga: Manga): Long = insertOrUpdateManga(manga)
    
    override suspend fun insertAllManga(mangaList: List<Manga>): List<Long> = withContext(Dispatchers.IO) {
        val entities = mangaList.map { it.toEntity() }
        val ids = mangaDao.insertAllManga(entities)
        
        // 批量更新缓存
        mangaList.forEachIndexed { index, manga ->
            val id = ids.getOrNull(index) ?: manga.id
            val updatedManga = manga.copy(id = if (manga.id == 0L) id else manga.id)
            updateCache(updatedManga)
            filePathCache[manga.filePath] = updatedManga
        }
        
        ids
    }
    
    override suspend fun updateReadingProgress(mangaId: Long, currentPage: Int, status: ReadingStatus) {
        withContext(Dispatchers.IO) {
            val manga = getMangaById(mangaId) ?: return@withContext
            val pageCount = manga.pageCount
            val progress = if (pageCount > 0) currentPage.toFloat() / pageCount.toFloat() else 0f
            val isCompleted = status == ReadingStatus.COMPLETED
            
            mangaDao.updateReadingProgress(mangaId, currentPage, progress, isCompleted, System.currentTimeMillis())
            
            // 更新缓存中的数据
            val updatedManga = manga.copy(
                currentPage = currentPage,
                readingStatus = status,
                lastRead = System.currentTimeMillis()
            )
            updateCache(updatedManga)
        }
    }
    
    override suspend fun toggleFavorite(mangaId: Long) {
        withContext(Dispatchers.IO) {
            mangaDao.toggleFavorite(mangaId)
            
            // 更新缓存
            val cachedManga = mangaCache[mangaId]
            if (cachedManga != null) {
                val updatedManga = cachedManga.copy(isFavorite = !cachedManga.isFavorite)
                updateCache(updatedManga)
            }
        }
    }
    
    override suspend fun updateRating(mangaId: Long, rating: Float) {
        withContext(Dispatchers.IO) {
            mangaDao.updateRating(mangaId, rating)
            
            // 更新缓存
            val cachedManga = mangaCache[mangaId]
            if (cachedManga != null) {
                val updatedManga = cachedManga.copy(rating = rating)
                updateCache(updatedManga)
            }
        }
    }
    
    override suspend fun deleteManga(manga: Manga) {
        withContext(Dispatchers.IO) {
            val entity = manga.toEntity()
            mangaDao.deleteManga(entity)
            
            // 清除缓存
            mangaCache.remove(manga.id)
            filePathCache.remove(manga.filePath)
            cacheTimestamps.remove(manga.id)
        }
    }
    
    override suspend fun deleteAllManga(mangaList: List<Manga>) {
        withContext(Dispatchers.IO) {
            val entities = mangaList.map { it.toEntity() }
            mangaDao.deleteAllManga(entities)
            
            // 批量清除缓存
            mangaList.forEach { manga ->
                mangaCache.remove(manga.id)
                filePathCache.remove(manga.filePath)
                cacheTimestamps.remove(manga.id)
            }
        }
    }
    
    override fun getMangaCount(): Flow<Int> = mangaDao.getMangaCount()
    
    override fun getFavoriteCount(): Flow<Int> = mangaDao.getFavoriteCount()
    
    override fun getCompletedCount(): Flow<Int> = mangaDao.getCompletedCount()

    override suspend fun getCover(manga: Manga): Bitmap? = withContext(Dispatchers.IO) {
        // TODO: 实现封面提取逻辑
        null
    }
    
    // ========== 缓存管理方法 ==========
    
    private fun updateCache(manga: Manga) {
        mangaCache[manga.id] = manga
        cacheTimestamps[manga.id] = System.currentTimeMillis()
    }
    
    private fun getCachedManga(id: Long): Manga? {
        return if (isCacheExpired(id)) {
            mangaCache.remove(id)
            cacheTimestamps.remove(id)
            null
        } else {
            mangaCache[id]
        }
    }
    
    private fun isCacheExpired(id: Long): Boolean {
        val timestamp = cacheTimestamps[id] ?: return true
        return System.currentTimeMillis() - timestamp > cacheExpirationTime
    }
    
    /**
     * 清除所有缓存
     */
    fun clearCache() {
        mangaCache.clear()
        filePathCache.clear()
        cacheTimestamps.clear()
    }
    
    /**
     * 清除过期缓存
     */
    fun clearExpiredCache() {
        val currentTime = System.currentTimeMillis()
        val expiredIds = cacheTimestamps.entries
            .filter { currentTime - it.value > cacheExpirationTime }
            .map { it.key }
        
        expiredIds.forEach { id ->
            val manga = mangaCache.remove(id)
            cacheTimestamps.remove(id)
            manga?.let { filePathCache.remove(it.filePath) }
        }
    }
}

// ========== 扩展函数：数据转换优化 ==========

/**
 * MangaEntity 转换为 Manga 领域模型
 * 优化：减少重复的状态判断逻辑
 */
private fun MangaEntity.toDomain(): Manga {
    val status = when {
        isCompleted -> ReadingStatus.COMPLETED
        currentPage > 1 -> ReadingStatus.READING
        else -> ReadingStatus.UNREAD
    }
    
    return Manga(
        id = id,
        title = title,
        author = author ?: "",
        description = description ?: "",
        filePath = filePath,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        isFavorite = isFavorite,
        dateAdded = dateAdded,
        lastRead = if (lastRead > 0) lastRead else null,
        rating = rating,
        coverImagePath = coverPath,
        readingStatus = status,
        tags = emptyList()
    )
}

/**
 * Manga 领域模型转换为 MangaEntity
 * 优化：统一时间戳处理
 */
private fun Manga.toEntity(): MangaEntity {
    val currentTime = System.currentTimeMillis()
    val progress = if (pageCount > 0) currentPage.toFloat() / pageCount.toFloat() else 0f
    
    return MangaEntity(
        id = id,
        title = title,
        author = author,
        description = description,
        filePath = filePath,
        fileSize = fileSize,
        pageCount = pageCount,
        currentPage = currentPage,
        isFavorite = isFavorite,
        dateAdded = dateAdded,
        lastRead = lastRead ?: 0L,
        rating = rating,
        coverPath = coverImagePath,
        readingProgress = progress,
        isCompleted = readingStatus == ReadingStatus.COMPLETED,
        format = filePath.substringAfterLast('.', ""),
        readingTime = 0,
        createdAt = if (id == 0L) currentTime else dateAdded,
        updatedAt = currentTime
    )
}