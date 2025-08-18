package com.easycomic.ui_bookshelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase
import com.easycomic.domain.usecase.manga.ImportComicsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

open class BookshelfViewModel(
    private val getAllMangaUseCase: GetAllMangaUseCase,
    private val importComicsUseCase: ImportComicsUseCase
) : ViewModel() {

    private val _comics = MutableStateFlow<List<Manga>>(emptyList())
    open fun getComics(): StateFlow<List<Manga>> = _comics.asStateFlow()

    init {
        loadComics()
    }

    private fun loadComics() {
        viewModelScope.launch {
            getAllMangaUseCase()
                .catch { e ->
                    Timber.e(e, "Failed to load comics")
                }
                .collect { mangaList ->
                    _comics.value = mangaList
                }
        }
    }

    fun importComic(path: String) {
        viewModelScope.launch {
            try {
                importComicsUseCase(File(path))
                loadComics()
            } catch (e: Exception) {
                Timber.e(e, "Failed to import comic")
            }
        }
    }
}