package com.easycomic.domain.usecase.manga

import com.easycomic.domain.repository.MangaRepository

/**
 * 标记漫画为已读用例
 */
class MarkMangasAsReadUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 标记单个漫画为已读（设置为最后一页）
     */
    suspend fun markSingleAsRead(mangaId: Long) {
        val manga = mangaRepository.getMangaById(mangaId)
        if (manga != null && manga.pageCount > 0) {
            val updatedManga = manga.copy(
                currentPage = manga.pageCount - 1, // 最后一页
                lastRead = System.currentTimeMillis()
            )
            mangaRepository.updateManga(updatedManga)
        }
    }
    
    /**
     * 批量标记漫画为已读
     */
    suspend fun markMultipleAsRead(mangaIds: List<Long>) {
        mangaIds.forEach { mangaId ->
            markSingleAsRead(mangaId)
        }
    }
    
    /**
     * 标记漫画为未读（重置到第一页）
     */
    suspend fun markAsUnread(mangaId: Long) {
        val manga = mangaRepository.getMangaById(mangaId)
        if (manga != null) {
            val updatedManga = manga.copy(
                currentPage = 0,
                lastRead = 0L // 设置为0而不是null
            )
            mangaRepository.updateManga(updatedManga)
        }
    }
    
    /**
     * 批量标记漫画为未读
     */
    suspend fun markMultipleAsUnread(mangaIds: List<Long>) {
        mangaIds.forEach { mangaId ->
            markAsUnread(mangaId)
        }
    }
}
