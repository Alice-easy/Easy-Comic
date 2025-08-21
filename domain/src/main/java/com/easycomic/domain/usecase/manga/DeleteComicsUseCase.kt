package com.easycomic.domain.usecase.manga

import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.first

/**
 * 删除漫画用例
 */
class DeleteComicsUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 删除单个漫画
     */
    suspend fun deleteSingle(mangaId: Long) {
        val manga = mangaRepository.getMangaById(mangaId)
        if (manga != null) {
            mangaRepository.deleteManga(manga)
        }
    }
    
    /**
     * 批量删除漫画
     */
    suspend fun deleteMultiple(mangaIds: List<Long>) {
        val mangasToDelete = mangaIds.mapNotNull { mangaId ->
            mangaRepository.getMangaById(mangaId)
        }
        if (mangasToDelete.isNotEmpty()) {
            mangaRepository.deleteAllManga(mangasToDelete)
        }
    }
    
    /**
     * 删除所有漫画（危险操作，需要确认）
     */
    suspend fun deleteAll() {
        // 获取所有漫画然后删除
        val mangaList = mangaRepository.getAllManga().first()
        if (mangaList.isNotEmpty()) {
            mangaRepository.deleteAllManga(mangaList)
        }
    }
}
