package com.easycomic.domain.di

import com.easycomic.domain.repository.BookmarkRepository
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.domain.repository.ReadingHistoryRepository
import com.easycomic.domain.usecase.manga.*
import org.koin.dsl.module

/**
 * 领域层依赖注入模块
 */
val domainModule = module {
    
    // 漫画用例
    factory { GetAllMangaUseCase(get<MangaRepository>()) }
    factory { GetMangaByIdUseCase(get<MangaRepository>()) }
    factory { SearchMangaUseCase(get<MangaRepository>()) }
    factory { GetFavoriteMangaUseCase(get<MangaRepository>()) }
    factory { GetRecentMangaUseCase(get<MangaRepository>()) }
    factory { GetMangaByStatusUseCase(get<MangaRepository>()) }
    factory { InsertOrUpdateMangaUseCase(get<MangaRepository>()) }
    factory { UpdateReadingProgressUseCase(get<MangaRepository>()) }
    factory { ToggleFavoriteUseCase(get<MangaRepository>()) }
    factory { UpdateRatingUseCase(get<MangaRepository>()) }
    factory { DeleteMangaUseCase(get<MangaRepository>()) }
    factory { DeleteAllMangaUseCase(get<MangaRepository>()) }
}