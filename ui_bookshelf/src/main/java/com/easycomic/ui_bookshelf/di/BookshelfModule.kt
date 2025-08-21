package com.easycomic.ui_bookshelf.di

import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.domain.repository.ComicImportRepository
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase
import com.easycomic.domain.usecase.manga.ImportComicsUseCase
import com.easycomic.ui_bookshelf.BookshelfViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val bookshelfModule = module {
    viewModel {
        BookshelfViewModel(
            getAllMangaUseCase = get(),
            importComicsUseCase = get(),
            comicImportRepository = get<ComicImportRepository>() as ComicImportRepositoryImpl
        )
    }
}