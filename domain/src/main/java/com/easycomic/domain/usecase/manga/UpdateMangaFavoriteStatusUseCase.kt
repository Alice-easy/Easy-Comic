package com.easycomic.domain.usecase.manga

import com.easycomic.domain.repository.MangaRepository

/**
 * 更新漫画收藏状态用例
 */
class UpdateMangaFavoriteStatusUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 更新单个漫画的收藏状态
     */
    suspend fun updateSingle(mangaId: Long, isFavorite: Boolean) {
        val manga = mangaRepository.getMangaById(mangaId)
        if (manga != null) {
            val updatedManga = manga.copy(isFavorite = isFavorite)
            mangaRepository.updateManga(updatedManga)
        }
    }
    
    /**
     * 批量更新漫画的收藏状态
     */
    suspend fun updateMultiple(mangaIds: List<Long>, isFavorite: Boolean) {
        mangaIds.forEach { mangaId ->
            updateSingle(mangaId, isFavorite)
        }
    }
    
    /**
     * 切换漫画的收藏状态
     */
    suspend fun toggleFavorite(mangaId: Long) {
        val manga = mangaRepository.getMangaById(mangaId)
        if (manga != null) {
            val updatedManga = manga.copy(isFavorite = !manga.isFavorite)
            mangaRepository.updateManga(updatedManga)
        }
    }
}
