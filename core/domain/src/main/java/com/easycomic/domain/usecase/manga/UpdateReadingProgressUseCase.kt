package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.repository.MangaRepository

/**
 * 更新阅读进度用例
 * 
 * @property mangaRepository 漫画仓库接口
 */
class UpdateReadingProgressUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 更新漫画阅读进度
     * 
     * @param mangaId 漫画ID
     * @param currentPage 当前页码
     * @param status 阅读状态
     */
    suspend operator fun invoke(mangaId: Long, currentPage: Int, status: ReadingStatus) {
        mangaRepository.updateReadingProgress(mangaId, currentPage, status)
    }
}