package com.easycomic.domain.usecase.manga

import com.easycomic.data.repository.MangaRepository
import javax.inject.Inject

class UpdateMangaUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(manga: com.easycomic.core.database.Manga) = mangaRepository.updateManga(manga)
    
    suspend fun updateFavoriteStatus(mangaId: Long, isFavorite: Boolean) = 
        mangaRepository.updateFavoriteStatus(mangaId, isFavorite)
    
    suspend fun updateReadingProgress(mangaId: Long, page: Int, totalPages: Int) = 
        mangaRepository.updateReadingProgress(mangaId, page, totalPages)
}