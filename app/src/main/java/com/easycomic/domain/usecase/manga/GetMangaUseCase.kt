package com.easycomic.domain.usecase.manga

import com.easycomic.core.database.Manga
import com.easycomic.data.repository.MangaRepository
import javax.inject.Inject

class GetMangaUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    suspend operator fun invoke(id: Long): Manga? = mangaRepository.getMangaById(id)
    
    suspend fun getMangaByFilePath(filePath: String): Manga? = mangaRepository.getMangaByFilePath(filePath)
}