package com.easycomic.di

import com.easycomic.fakes.FakeComicImportRepository
import com.easycomic.fakes.FakeComicParserFactory
import com.easycomic.fakes.FakeMangaRepository
import com.easycomic.ui_bookshelf.BookshelfViewModel
import com.easycomic.ui_reader.ReaderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin module for instrumentation tests.
 * It provides fake repositories and the real UseCases and ViewModel
 * to test the UI layer in a controlled environment.
 *
 * Using fully qualified names to avoid build/import issues.
 */
object TestAppModule {
    val module = module {
        // Provide Fake Repositories
        single<com.easycomic.domain.repository.MangaRepository> { FakeMangaRepository() }
        single<com.easycomic.domain.repository.ComicImportRepository> { FakeComicImportRepository() }
        single<com.easycomic.domain.parser.ComicParserFactory> { FakeComicParserFactory() }

        // Provide real UseCases, which will get the Fake Repositories
        factory { com.easycomic.domain.usecase.manga.GetAllMangaUseCase(get()) }
        factory { com.easycomic.domain.usecase.manga.ImportComicsUseCase(get()) }
        factory { com.easycomic.domain.usecase.manga.GetMangaByIdUseCase(get()) }
        factory { com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase(get()) }

        // Provide real ViewModels, which will get the real UseCases
        viewModel { BookshelfViewModel(get(), get()) }
        viewModel { (savedStateHandle: androidx.lifecycle.SavedStateHandle) -> 
            ReaderViewModel(savedStateHandle, get(), get(), get()) 
        }
    }
}
