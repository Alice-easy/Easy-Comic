package com.easycomic.domain.usecase.bookmark

import com.easycomic.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(mangaId: Long): Flow<List<com.easycomic.core.database.Bookmark>> = 
        bookmarkRepository.getBookmarksForManga(mangaId)
    
    fun getAllBookmarks(): Flow<List<com.easycomic.core.database.Bookmark>> = 
        bookmarkRepository.getAllBookmarks()
    
    fun getRecentBookmarksForManga(mangaId: Long, limit: Int = 5): Flow<List<com.easycomic.core.database.Bookmark>> = 
        bookmarkRepository.getRecentBookmarksForManga(mangaId, limit)
}