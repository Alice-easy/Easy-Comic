package com.easycomic.ui_bookshelf.di

import com.easycomic.domain.usecase.manga.GetAllMangaUseCase
import com.easycomic.domain.usecase.manga.ImportComicsUseCase
import com.easycomic.ui_bookshelf.BookshelfViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val bookshelfModule = module {
    viewModel {
        BookshelfViewModel(
            getAllMangaUseCase = get(),
            importComicsUseCase = get()
        )
    }
}