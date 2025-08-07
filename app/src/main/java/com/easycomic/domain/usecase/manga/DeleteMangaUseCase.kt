package com.easycomic.domain.usecase.manga

import com.easycomic.data.repository.MangaRepository
import javax.inject.Inject

class DeleteMangaUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(manga: com.easycomic.core.database.Manga) = mangaRepository.deleteManga(manga)
    
    suspend fun deleteMangaById(mangaId: Long) = mangaRepository.deleteMangaById(mangaId)
}