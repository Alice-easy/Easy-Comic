package com.easycomic.ui.di

import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * UI层依赖注入模块
 */
val uiModule = module {
    
    // BookshelfViewModel
    viewModel {
        BookshelfViewModel(
            getAllMangaUseCase = get(),
            searchMangaUseCase = get(),
            getFavoriteMangaUseCase = get(),
            getRecentMangaUseCase = get(),
            deleteMangaUseCase = get(),
            deleteAllMangaUseCase = get(),
            toggleFavoriteUseCase = get()
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