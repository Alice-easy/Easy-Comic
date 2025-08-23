package com.easycomic.ui_reader.di

import androidx.lifecycle.SavedStateHandle
import com.easycomic.data.parser.ComicParserFactoryImpl
import com.easycomic.domain.parser.ComicParserFactory
import com.easycomic.ui_reader.ReaderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val readerModule = module {
    // Factory for creating comic parsers
    factory<ComicParserFactory> { ComicParserFactoryImpl() }

    viewModel { (savedStateHandle: SavedStateHandle) ->
        ReaderViewModel(
            savedStateHandle = savedStateHandle,
            getMangaByIdUseCase = get(),
            updateReadingProgressUseCase = get(),
            comicParserFactory = get()
        )
    }
}
