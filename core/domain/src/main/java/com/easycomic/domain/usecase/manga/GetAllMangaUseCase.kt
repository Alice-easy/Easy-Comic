package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取所有漫画用例
 * 
 * @property mangaRepository 漫画仓库接口
 */
class GetAllMangaUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 获取所有漫画
     * 
     * @return Flow<List<Manga>> 漫画列表流
     */
    operator fun invoke(): Flow<List<Manga>> = mangaRepository.getAllManga()
}