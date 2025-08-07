package com.easycomic.domain.usecase.bookmark

import com.easycomic.data.repository.BookmarkRepository
import javax.inject.Inject

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(bookmark: com.easycomic.core.database.Bookmark): Long = 
        bookmarkRepository.addBookmark(bookmark)
    
    suspend fun createBookmark(mangaId: Long, page: Int, name: String, note: String? = null): Long =
        bookmarkRepository.createBookmark(mangaId, page, name, note)
}