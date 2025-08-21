package com.easycomic.ui.di

import coil.ImageLoader
import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderViewModel
import com.easycomic.ui.theme.ThemeViewModel
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
    
    // ThemeViewModel
    viewModel {
        ThemeViewModel(
            getThemePreferenceUseCase = get(),
            updateThemePreferenceUseCase = get()
        )
    }
    
    // BookshelfViewModel
    viewModel {
        BookshelfViewModel(
            getAllMangaUseCase = get(),
            importComicsUseCase = get(),
            comicImportRepository = get(),
            deleteComicsUseCase = get(),
            updateMangaFavoriteStatusUseCase = get(),
            markMangasAsReadUseCase = get()
        )
    }
    
    // ReaderViewModel
    viewModel { params ->
        ReaderViewModel(
            savedStateHandle = params.get(),
            getMangaByIdUseCase = get(),
            updateReadingProgressUseCase = get()
        )
    }
}
