package com.easycomic.ui.di

import coil.ImageLoader
import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderViewModel
import com.easycomic.utils.MangaCoverFetcher
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * UI层依赖注入模块
 */
val uiModule = module {
    
    // ImageLoader for Coil
    single {
        ImageLoader.Builder(androidContext())
            .components {
                add(MangaCoverFetcher.Factory(get()))
            }
            .build()
    }
    
    // BookshelfViewModel
    viewModel {
        BookshelfViewModel(
            getAllMangaUseCase = get(),
            searchMangaUseCase = get(),
            getFavoriteMangaUseCase = get(),
            getRecentMangaUseCase = get(),
            deleteMangaUseCase = get(),
            deleteAllMangaUseCase = get(),
            toggleFavoriteUseCase = get(),
            importComicsUseCase = get()
        )
    }
    
    // ReaderViewModel
    viewModel {
        ReaderViewModel(
            getMangaByIdUseCase = get(),
            updateReadingProgressUseCase = get(),
            bookmarkRepository = get(),
            readingHistoryRepository = get()
        )
    }
}