package com.easycomic.domain.di

import com.easycomic.domain.usecase.manga.*
import org.koin.dsl.module

val domainModule = module {
    factory { GetAllMangaUseCase(get()) }
    factory { GetMangaByIdUseCase(get()) }
    factory { SearchMangaUseCase(get()) }
    factory { GetFavoriteMangaUseCase(get()) }
    factory { GetRecentMangaUseCase(get()) }
    factory { InsertOrUpdateMangaUseCase(get()) }
    factory { UpdateReadingProgressUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { UpdateRatingUseCase(get()) }
    factory { DeleteMangaUseCase(get()) }
    factory { DeleteAllMangaUseCase(get()) }
    factory { ImportComicsUseCase(get()) }
    factory { GetCoverUseCase(get()) }
}