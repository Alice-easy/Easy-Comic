package com.easycomic.domain.di

import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.data.service.ComicImportService
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.SearchMangaUseCase
import com.easycomic.domain.usecase.manga.GetFavoriteMangaUseCase
import com.easycomic.domain.usecase.manga.GetRecentMangaUseCase
import com.easycomic.domain.usecase.manga.GetMangaByStatusUseCase
import com.easycomic.domain.usecase.manga.InsertOrUpdateMangaUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
import com.easycomic.domain.usecase.manga.ToggleFavoriteUseCase
import com.easycomic.domain.usecase.manga.UpdateRatingUseCase
import com.easycomic.domain.usecase.manga.DeleteMangaUseCase
import com.easycomic.domain.usecase.manga.DeleteAllMangaUseCase
import com.easycomic.domain.usecase.manga.ImportComicUseCase
import com.easycomic.domain.usecase.manga.BatchImportComicsUseCase
import com.easycomic.domain.usecase.manga.MonitorImportProgressUseCase
import com.easycomic.domain.usecase.manga.UpdateImportProgressUseCase
import com.easycomic.domain.usecase.manga.ImportProgressHolder

/**
 * 领域层依赖注入模块 - 使用 Koin
 * 
 * 注意：这个文件保留用于兼容性，实际的依赖注入现在通过 Koin 处理
 * 所有依赖都在 KoinModules.kt 中定义
 */
object DomainModule {
    
    // 漫画用例 - 已移动到Koin模块
    fun provideGetAllMangaUseCase(mangaRepository: MangaRepository): GetAllMangaUseCase = GetAllMangaUseCase(mangaRepository)
    
    fun provideGetMangaByIdUseCase(mangaRepository: MangaRepository): GetMangaByIdUseCase = GetMangaByIdUseCase(mangaRepository)
    
    fun provideSearchMangaUseCase(mangaRepository: MangaRepository): SearchMangaUseCase = SearchMangaUseCase(mangaRepository)
    
    fun provideGetFavoriteMangaUseCase(mangaRepository: MangaRepository): GetFavoriteMangaUseCase = GetFavoriteMangaUseCase(mangaRepository)
    
    fun provideGetRecentMangaUseCase(mangaRepository: MangaRepository): GetRecentMangaUseCase = GetRecentMangaUseCase(mangaRepository)
    
    fun provideGetMangaByStatusUseCase(mangaRepository: MangaRepository): GetMangaByStatusUseCase = GetMangaByStatusUseCase(mangaRepository)
    
    fun provideInsertOrUpdateMangaUseCase(mangaRepository: MangaRepository): InsertOrUpdateMangaUseCase = InsertOrUpdateMangaUseCase(mangaRepository)
    
    fun provideUpdateReadingProgressUseCase(mangaRepository: MangaRepository): UpdateReadingProgressUseCase = UpdateReadingProgressUseCase(mangaRepository)
    
    fun provideToggleFavoriteUseCase(mangaRepository: MangaRepository): ToggleFavoriteUseCase = ToggleFavoriteUseCase(mangaRepository)
    
    fun provideUpdateRatingUseCase(mangaRepository: MangaRepository): UpdateRatingUseCase = UpdateRatingUseCase(mangaRepository)
    
    fun provideDeleteMangaUseCase(mangaRepository: MangaRepository): DeleteMangaUseCase = DeleteMangaUseCase(mangaRepository)
    
    fun provideDeleteAllMangaUseCase(mangaRepository: MangaRepository): DeleteAllMangaUseCase = DeleteAllMangaUseCase(mangaRepository)
    
    // 导入相关用例 - 已移动到Koin模块
    fun provideImportComicUseCase(comicImportService: ComicImportService): ImportComicUseCase = ImportComicUseCase(comicImportService)
    
    fun provideBatchImportComicsUseCase(
        importComicUseCase: ImportComicUseCase,
        importProgressHolder: ImportProgressHolder
    ): BatchImportComicsUseCase = BatchImportComicsUseCase(importComicUseCase, importProgressHolder)
    
    fun provideMonitorImportProgressUseCase(importProgressHolder: ImportProgressHolder): MonitorImportProgressUseCase = MonitorImportProgressUseCase(importProgressHolder)
    
    fun provideUpdateImportProgressUseCase(importProgressHolder: ImportProgressHolder): UpdateImportProgressUseCase = UpdateImportProgressUseCase(importProgressHolder)
}