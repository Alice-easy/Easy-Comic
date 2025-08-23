package com.easycomic.domain.usecase.manga

import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository

/**
 * 根据ID获取漫画用例
 * 
 * @property mangaRepository 漫画仓库接口
 */
class GetMangaByIdUseCase(
    private val mangaRepository: MangaRepository
) {
    
    /**
     * 根据ID获取漫画
     * 
     * @param id 漫画ID
     * @return Manga? 漫画对象，如果不存在则返回null
     */
    suspend operator fun invoke(id: Long): Manga? = mangaRepository.getMangaById(id)
}