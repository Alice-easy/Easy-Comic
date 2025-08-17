package com.easycomic.ui_reader.di

import com.easycomic.ui_reader.ReaderViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

import androidx.lifecycle.SavedStateHandle

val readerModule = module {
    viewModel { (savedStateHandle: SavedStateHandle) ->
        ReaderViewModel(
            savedStateHandle = savedStateHandle,
            getMangaByIdUseCase = get(),
            updateReadingProgressUseCase = get()
        )
    }
}
