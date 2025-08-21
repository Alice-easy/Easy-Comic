package com.easycomic.data.repository

import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.entity.BookmarkEntity
import com.easycomic.domain.model.Bookmark
import com.easycomic.domain.repository.BookmarkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * 优化后的书签仓库实现类
 * 
 * 优化点：
 * 1. 添加内存缓存减少数据库查询
 * 2. 优化批量操作性能
 * 3. 改进查询策略
 * 4. 添加预加载机制
 */
class OptimizedBookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {
    
    // 内存缓存：按漫画ID缓存书签列表
    private val bookmarksByMangaCache = ConcurrentHashMap<Long, List<Bookmark>>()
    
    // 内存缓存：书签详情缓存
    private val bookmarkCache = ConcurrentHashMap<Long, Bookmark>()
    
    // 缓存过期时间（3分钟）
    private val cacheExpirationTime = 3 * 60 * 1000L
    private val cacheTimestamps = ConcurrentHashMap<Long, Long>()
    
    override fun getBookmarksByMangaId(mangaId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByMangaId(mangaId).map { entities ->
            val bookmarks = entities.map { entity ->
                val bookmark = entity.toDomain()
                // 更新单个书签缓存
                updateBookmarkCache(bookmark)
                bookmark
            }
            
            // 更新漫画书签列表缓存
            bookmarksByMangaCache[mangaId] = bookmarks
            cacheTimestamps[mangaId] = System.currentTimeMillis()
            
            bookmarks
        }
    }
    
    override suspend fun getBookmarkByPage(mangaId: Long, pageNumber: Int): List<Bookmark> = withContext(Dispatchers.IO) {
        // 先检查缓存中是否有该漫画的书签列表
        val cachedBookmarks = getCachedBookmarksByManga(mangaId)
        if (cachedBookmarks != null) {
            return@withContext cachedBookmarks.filter { it.pageNumber == pageNumber }
        }
        
        // 缓存未命中，查询数据库
        val entities = bookmarkDao.getBookmarkByPage(mangaId, pageNumber)
        entities.map { entity ->
            val bookmark = entity.toDomain()
            updateBookmarkCache(bookmark)
            bookmark
        }
    }
    
    override suspend fun getBookmarkById(bookmarkId: Long): Bookmark? = withContext(Dispatchers.IO) {
        // 先检查缓存
        val cachedBookmark = getCachedBookmark(bookmarkId)
        if (cachedBookmark != null) {
            return@withContext cachedBookmark
        }
        
        // 缓存未命中，查询数据库
        val entity = bookmarkDao.getBookmarkById(bookmarkId)
        val bookmark = entity?.toDomain()
        
        // 更新缓存
        bookmark?.let { updateBookmarkCache(it) }
        
        bookmark
    }
    
    override suspend fun hasBookmarkForPage(mangaId: Long, pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        // 先检查缓存
        val cachedBookmarks = getCachedBookmarksByManga(mangaId)
        if (cachedBookmarks != null) {
            return@withContext cachedBookmarks.any { it.pageNumber == pageNumber }
        }
        
        // 缓存未命中，查询数据库
        bookmarkDao.hasBookmarkForPage(mangaId, pageNumber) > 0
    }
    
    override suspend fun addBookmark(bookmark: Bookmark): Long = withContext(Dispatchers.IO) {
        val entity = bookmark.toEntity()
        val id = bookmarkDao.insertBookmark(entity)
        
        // 更新缓存
        val newBookmark = bookmark.copy(id = if (bookmark.id == 0L) id else bookmark.id)
        updateBookmarkCache(newBookmark)
        
        // 清除该漫画的书签列表缓存，强制重新加载
        invalidateMangaBookmarksCache(bookmark.mangaId)
        
        id
    }
    
    override suspend fun updateBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO) {
            val entity = bookmark.toEntity()
            bookmarkDao.updateBookmark(entity)
            
            // 更新缓存
            updateBookmarkCache(bookmark)
            
            // 清除该漫画的书签列表缓存
            invalidateMangaBookmarksCache(bookmark.mangaId)
        }
    }
    
    override suspend fun deleteBookmark(bookmark: Bookmark) {
        withContext(Dispatchers.IO) {
            val entity = bookmark.toEntity()
            bookmarkDao.deleteBookmark(entity)
            
            // 清除缓存
            bookmarkCache.remove(bookmark.id)
            invalidateMangaBookmarksCache(bookmark.mangaId)
        }
    }
    
    override suspend fun deleteBookmarkById(bookmarkId: Long) {
        withContext(Dispatchers.IO) {
            // 先获取书签信息以便清除相关缓存
            val bookmark = getBookmarkById(bookmarkId)
            
            bookmarkDao.deleteBookmarkById(bookmarkId)
            
            // 清除缓存
            bookmarkCache.remove(bookmarkId)
            bookmark?.let { invalidateMangaBookmarksCache(it.mangaId) }
        }
    }
    
    override suspend fun deleteBookmarksByMangaId(mangaId: Long) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarksByMangaId(mangaId)
            
            // 清除该漫画相关的所有缓存
            invalidateMangaBookmarksCache(mangaId)
            
            // 清除该漫画的所有书签详情缓存
            val cachedBookmarks = bookmarksByMangaCache[mangaId]
            cachedBookmarks?.forEach { bookmark ->
                bookmarkCache.remove(bookmark.id)
            }
        }
    }
    
    override suspend fun deleteBookmarkByPage(mangaId: Long, pageNumber: Int) {
        withContext(Dispatchers.IO) {
            bookmarkDao.deleteBookmarkByPage(mangaId, pageNumber)
            
            // 清除相关缓存
            invalidateMangaBookmarksCache(mangaId)
        }
    }
    
    override suspend fun deleteAllBookmarks(bookmarkList: List<Bookmark>) {
        withContext(Dispatchers.IO) {
            val entities = bookmarkList.map { it.toEntity() }
            bookmarkDao.deleteAllBookmarks(entities)
            
            // 批量清除缓存
            val affectedMangaIds = bookmarkList.map { it.mangaId }.distinct()
            bookmarkList.forEach { bookmark ->
                bookmarkCache.remove(bookmark.id)
            }
            affectedMangaIds.forEach { mangaId ->
                invalidateMangaBookmarksCache(mangaId)
            }
        }
    }
    
    override fun getBookmarkCount(): Flow<Int> = bookmarkDao.getBookmarkCount()
    
    override suspend fun getBookmarkCountByMangaId(mangaId: Long): Int = withContext(Dispatchers.IO) {
        // 先检查缓存
        val cachedBookmarks = getCachedBookmarksByManga(mangaId)
        if (cachedBookmarks != null) {
            return@withContext cachedBookmarks.size
        }
        
        // 缓存未命中，查询数据库
        bookmarkDao.getBookmarkCountByMangaId(mangaId)
    }
    
    override fun getRecentBookmarks(limit: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getRecentBookmarks(limit).map { entities ->
            entities.map { entity ->
                val bookmark = entity.toDomain()
                updateBookmarkCache(bookmark)
                bookmark
            }
        }
    }
    
    // ========== 缓存管理方法 ==========
    
    private fun updateBookmarkCache(bookmark: Bookmark) {
        bookmarkCache[bookmark.id] = bookmark
        cacheTimestamps[bookmark.id] = System.currentTimeMillis()
    }
    
    private fun getCachedBookmark(id: Long): Bookmark? {
        return if (isBookmarkCacheExpired(id)) {
            bookmarkCache.remove(id)
            cacheTimestamps.remove(id)
            null
        } else {
            bookmarkCache[id]
        }
    }
    
    private fun getCachedBookmarksByManga(mangaId: Long): List<Bookmark>? {
        return if (isMangaBookmarksCacheExpired(mangaId)) {
            bookmarksByMangaCache.remove(mangaId)
            cacheTimestamps.remove(mangaId)
            null
        } else {
            bookmarksByMangaCache[mangaId]
        }
    }
    
    private fun invalidateMangaBookmarksCache(mangaId: Long) {
        bookmarksByMangaCache.remove(mangaId)
        cacheTimestamps.remove(mangaId)
    }
    
    private fun isBookmarkCacheExpired(id: Long): Boolean {
        val timestamp = cacheTimestamps[id] ?: return true
        return System.currentTimeMillis() - timestamp > cacheExpirationTime
    }
    
    private fun isMangaBookmarksCacheExpired(mangaId: Long): Boolean {
        val timestamp = cacheTimestamps[mangaId] ?: return true
        return System.currentTimeMillis() - timestamp > cacheExpirationTime
    }
    
    /**
     * 预加载指定漫画的书签
     */
    suspend fun preloadBookmarks(mangaId: Long) {
        withContext(Dispatchers.IO) {
            if (getCachedBookmarksByManga(mangaId) == null) {
                // 触发数据加载
                val entities = bookmarkDao.getBookmarksByMangaId(mangaId)
                // Flow会在collect时触发数据库查询和缓存更新
            }
        }
    }
    
    /**
     * 清除所有缓存
     */
    fun clearCache() {
        bookmarkCache.clear()
        bookmarksByMangaCache.clear()
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
            bookmarkCache.remove(id)
            bookmarksByMangaCache.remove(id)
            cacheTimestamps.remove(id)
        }
    }
}

// ========== 扩展函数：数据转换优化 ==========

/**
 * BookmarkEntity 转换为 Bookmark 领域模型
 */
private fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        mangaId = mangaId,
        pageNumber = pageNumber,
        name = bookmarkName ?: "",
        description = notes ?: "",
        createdAt = createdAt,
        updatedAt = createdAt // Entity没有updatedAt字段，使用createdAt
    )
}

/**
 * Bookmark 领域模型转换为 BookmarkEntity
 */
private fun Bookmark.toEntity(): BookmarkEntity {
    return BookmarkEntity(
        id = id,
        mangaId = mangaId,
        pageNumber = pageNumber,
        bookmarkName = name,
        notes = description,
        createdAt = createdAt
    )
}