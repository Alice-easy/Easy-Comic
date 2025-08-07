package com.easycomic.domain.usecase.manga

import com.easycomic.core.database.Manga
import com.easycomic.data.repository.MangaRepository
import javax.inject.Inject

class AddMangaUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(manga: Manga): Long = mangaRepository.insertManga(manga)
}