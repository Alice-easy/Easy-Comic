package com.easycomic.domain.usecase.manga

import com.easycomic.domain.repository.ComicImportRepository
import java.io.File

/**
 * 导入漫画用例
 * 
 * @property comicImportRepository 漫画导入仓库接口
 */
class ImportComicsUseCase(
    private val comicImportRepository: ComicImportRepository
) {
    
    /**
     * 导入漫画文件
     * 
     * @param directory 目录文件
     */
    suspend operator fun invoke(directory: File) {
        comicImportRepository.importComics(directory)
    }
}