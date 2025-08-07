package com.easycomic.domain.usecase.manga

import com.easycomic.core.database.Manga
import com.easycomic.data.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMangaListUseCase @Inject constructor(
    private val mangaRepository: MangaRepository
) {
    operator fun invoke(): Flow<List<Manga>> = mangaRepository.getAllManga()
    
    fun getFavoriteManga(): Flow<List<Manga>> = mangaRepository.getFavoriteManga()
    
    fun getRecentManga(limit: Int = 10): Flow<List<Manga>> = mangaRepository.getRecentManga(limit)
    
    fun getInProgressManga(): Flow<List<Manga>> = mangaRepository.getInProgressManga()
    
    fun getCompletedManga(): Flow<List<Manga>> = mangaRepository.getCompletedManga()
    
    fun searchManga(query: String): Flow<List<Manga>> = mangaRepository.searchManga(query)
}