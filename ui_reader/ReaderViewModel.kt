package com.easycomic.ui.reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.data.entity.ReadingStatus
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
import com.easycomic.utils.ComicParser
import com.easycomic.utils.RarComicParser
import com.easycomic.utils.ZipComicParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

class ReaderViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMangaByIdUseCase: GetMangaByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase
) : ViewModel() {

    private val mangaId: Long = savedStateHandle.get<Long>("mangaId")!!

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var comicParser: ComicParser? = null
    private var saveProgressJob: Job? = null

    init {
        loadManga()
    }

    private fun loadManga() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val manga = getMangaByIdUseCase(mangaId)
            if (manga == null) {
                _uiState.update { it.copy(isLoading = false, error = "无法加载漫画") }
                return@launch
            }

            comicParser = getParserForFile(File(manga.filePath))
            if (comicParser == null) {
                _uiState.update { it.copy(isLoading = false, error = "不支持的文件格式") }
                return@launch
            }

            _uiState.update {
                it.copy(
                    manga = manga,
                    pageCount = comicParser?.getPageCount() ?: 0,
                    currentPage = manga.currentPage,
                    isLoading = false
                )
            }
            loadPage(manga.currentPage)
        }
    }

    private fun loadPage(pageIndex: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingImage = true) }
            val bitmap = withContext(Dispatchers.IO) {
                comicParser?.getPageStream(pageIndex)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }
            if (bitmap != null) {
                _uiState.update { it.copy(currentPageBitmap = bitmap, isLoadingImage = false) }
            } else {
                _uiState.update { it.copy(isLoadingImage = false, error = "无法加载页面 $pageIndex") }
            }
        }
    }

    fun nextPage() {
        val nextPageIndex = _uiState.value.currentPage + 1
        if (nextPageIndex < _uiState.value.pageCount) {
            _uiState.update { it.copy(currentPage = nextPageIndex) }
            loadPage(nextPageIndex)
            saveProgress()
        }
    }

    fun previousPage() {
        val prevPageIndex = _uiState.value.currentPage - 1
        if (prevPageIndex >= 0) {
            _uiState.update { it.copy(currentPage = prevPageIndex) }
            loadPage(prevPageIndex)
            saveProgress()
        }
    }

    fun goToPage(pageIndex: Int) {
        if (pageIndex >= 0 && pageIndex < _uiState.value.pageCount) {
            _uiState.update { it.copy(currentPage = pageIndex) }
            loadPage(pageIndex)
            saveProgress()
        }
    }

    private fun saveProgress() {
        saveProgressJob?.cancel()
        saveProgressJob = viewModelScope.launch {
            delay(500) // Debounce
            val currentState = _uiState.value
            val status = if (currentState.currentPage == currentState.pageCount - 1) {
                ReadingStatus.COMPLETED
            } else {
                ReadingStatus.READING
            }
            updateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = mangaId,
                    currentPage = currentState.currentPage,
                    status = status
                )
            )
        }
    }

    private fun getParserForFile(file: File): ComicParser? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "zip", "cbz" -> ZipComicParser(file)
            "rar", "cbr" -> RarComicParser(file)
            else -> null
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        saveProgressJob?.cancel() // Ensure last progress is saved
        saveProgress()
        viewModelScope.launch(Dispatchers.IO) {
            comicParser?.close()
        }
    }
}

data class ReaderUiState(
    val isLoading: Boolean = true,
    val isLoadingImage: Boolean = false,
    val manga: Manga? = null,
    val currentPage: Int = 0,
    val pageCount: Int = 0,
    val currentPageBitmap: Bitmap? = null,
    val error: String? = null
)