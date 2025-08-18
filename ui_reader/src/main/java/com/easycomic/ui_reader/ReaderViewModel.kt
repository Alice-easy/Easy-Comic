package com.easycomic.ui_reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.domain.model.Manga
import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
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

enum class ReadingMode { FIT, FILL }
enum class ReadingDirection { HORIZONTAL, VERTICAL }

data class ReaderSettings(
    val readingMode: ReadingMode = ReadingMode.FIT,
    val readingDirection: ReadingDirection = ReadingDirection.HORIZONTAL,
    val isMenuVisible: Boolean = true
)

class ReaderViewModel(
    savedStateHandle: SavedStateHandle,
    private val getMangaByIdUseCase: GetMangaByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val comicParserFactory: ComicParserFactory
) : ViewModel() {

    private val mangaId: Long by lazy { savedStateHandle.get<Long>("mangaId")!! }

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
            try {
                val manga = getMangaByIdUseCase(mangaId)
                if (manga == null) {
                    _uiState.update { it.copy(isLoading = false, error = "无法加载漫画") }
                    return@launch
                }

                comicParser = comicParserFactory.create(File(manga.filePath))
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
            } catch (e: Exception) {
                Timber.e(e, "加载漫画失败")
                _uiState.update { it.copy(isLoading = false, error = "加载漫画失败: ${e.message}") }
            }
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
            delay(500) // Debounce to avoid rapid updates
            val currentState = _uiState.value
            if (currentState.manga == null || currentState.pageCount == 0) return@launch

            val isCompleted = currentState.currentPage >= currentState.pageCount - 1

            updateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = mangaId,
                    currentPage = currentState.currentPage,
                    pageCount = currentState.pageCount
                )
            )
        }
    }


    fun toggleMenu() {
        _uiState.update {
            it.copy(settings = it.settings.copy(isMenuVisible = !it.settings.isMenuVisible))
        }
    }

    fun setReadingMode(mode: ReadingMode) {
        _uiState.update {
            it.copy(settings = it.settings.copy(readingMode = mode))
        }
    }

    fun setReadingDirection(direction: ReadingDirection) {
        _uiState.update {
            it.copy(settings = it.settings.copy(readingDirection = direction))
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

    /**
     * Asynchronously gets the bitmap for a specific page.
     * Designed for use with vertical scrolling readers.
     */
    suspend fun getPageBitmap(pageIndex: Int): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                comicParser?.getPageStream(pageIndex)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load bitmap for page $pageIndex")
                null
            }
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
    val settings: ReaderSettings = ReaderSettings(),
    val error: String? = null
)
