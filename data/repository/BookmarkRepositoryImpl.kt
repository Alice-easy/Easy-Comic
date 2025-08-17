package com.easycomic.data.repository

import com.easycomic.data.dao.BookmarkDao
import com.easycomic.data.entity.BookmarkEntity
import com.easycomic.domain.model.Bookmark
import com.easycomic.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 书签仓库实现类
 */
class BookmarkRepositoryImpl(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {
    
    override fun getBookmarksByMangaId(mangaId: Long): Flow<List<Bookmark>> {
        return bookmarkDao.getBookmarksByMangaId(mangaId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getBookmarkByPage(mangaId: Long, pageNumber: Int): List<Bookmark> {
        return bookmarkDao.getBookmarkByPage(mangaId, pageNumber).map { it.toDomain() }
    }
    
    override suspend fun getBookmarkById(bookmarkId: Long): Bookmark? {
        return bookmarkDao.getBookmarkById(bookmarkId)?.toDomain()
    }
    
    override suspend fun hasBookmarkForPage(mangaId: Long, pageNumber: Int): Boolean {
        return bookmarkDao.hasBookmarkForPage(mangaId, pageNumber) > 0
    }
    
    override suspend fun addBookmark(bookmark: Bookmark): Long {
        val entity = bookmark.toEntity()
        return bookmarkDao.insertBookmark(entity)
    }
    
    override suspend fun updateBookmark(bookmark: Bookmark) {
        val entity = bookmark.toEntity()
        bookmarkDao.updateBookmark(entity)
    }
    
    override suspend fun deleteBookmark(bookmark: Bookmark) {
        val entity = bookmark.toEntity()
        bookmarkDao.deleteBookmark(entity)
    }
    
    override suspend fun deleteBookmarkById(bookmarkId: Long) {
        bookmarkDao.deleteBookmarkById(bookmarkId)
    }
    
    override suspend fun deleteBookmarksByMangaId(mangaId: Long) {
        bookmarkDao.deleteBookmarksByMangaId(mangaId)
    }
    
    override suspend fun deleteBookmarkByPage(mangaId: Long, pageNumber: Int) {
        bookmarkDao.deleteBookmarkByPage(mangaId, pageNumber)
    }
    
    override suspend fun deleteAllBookmarks(bookmarkList: List<Bookmark>) {
        val entities = bookmarkList.map { it.toEntity() }
        bookmarkDao.deleteAllBookmarks(entities)
    }
    
    override fun getBookmarkCount(): Flow<Int> {
        return bookmarkDao.getBookmarkCount()
    }
    
    override suspend fun getBookmarkCountByMangaId(mangaId: Long): Int {
        return bookmarkDao.getBookmarkCountByMangaId(mangaId)
    }
    
    override fun getRecentBookmarks(limit: Int): Flow<List<Bookmark>> {
        return bookmarkDao.getRecentBookmarks(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}

/**
 * BookmarkEntity 转换为 Bookmark 领域模型
 */
private fun BookmarkEntity.toDomain(): Bookmark {
    return Bookmark(
        id = id,
        mangaId = mangaId,
        pageNumber = pageNumber,
        name = bookmarkName ?: "", // Map entity's bookmarkName to domain's name
        description = notes ?: "", // Map entity's notes to domain's description
        createdAt = createdAt,
        updatedAt = createdAt // Entity doesn't have updated_at, use created_at
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
        bookmarkName = name, // Map domain's name to entity's bookmarkName
        notes = description, // Map domain's description to entity's notes
        createdAt = createdAt
    )
}
