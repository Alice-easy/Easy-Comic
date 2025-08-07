package com.easycomic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.core.database.Bookmark
import com.easycomic.core.database.Manga
import com.easycomic.core.database.ReadingHistory
import com.easycomic.domain.usecase.bookmark.*
import com.easycomic.domain.usecase.manga.GetMangaUseCase
import com.easycomic.domain.usecase.manga.UpdateMangaUseCase
import com.easycomic.domain.usecase.readinghistory.GetReadingHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getMangaUseCase: GetMangaUseCase,
    private val updateMangaUseCase: UpdateMangaUseCase,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val getReadingHistoryUseCase: GetReadingHistoryUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()
    
    fun loadManga(mangaId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val manga = getMangaUseCase(mangaId)
                if (manga != null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        manga = manga,
                        currentPage = manga.currentPage,
                        totalPages = manga.totalPages
                    ) }
                    loadBookmarks(mangaId)
                    loadReadingHistory(mangaId)
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Manga not found"
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load manga"
                ) }
            }
        }
    }
    
    private fun loadBookmarks(mangaId: Long) {
        viewModelScope.launch {
            getBookmarksUseCase(mangaId)
                .collect { bookmarks ->
                    _uiState.update { it.copy(bookmarks = bookmarks) }
                }
        }
    }
    
    private fun loadReadingHistory(mangaId: Long) {
        viewModelScope.launch {
            val history = getReadingHistoryUseCase.getHistoryForManga(mangaId)
            _uiState.update { it.copy(readingHistory = history) }
        }
    }
    
    fun onPageChanged(page: Int) {
        val manga = _uiState.value.manga ?: return
        val totalPages = _uiState.value.totalPages
        
        _uiState.update { it.copy(currentPage = page) }
        
        viewModelScope.launch {
            updateMangaUseCase.updateReadingProgress(manga.id, page, totalPages)
        }
    }
    
    fun onPreviousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0) {
            onPageChanged(currentPage - 1)
        }
    }
    
    fun onNextPage() {
        val currentPage = _uiState.value.currentPage
        val totalPages = _uiState.value.totalPages
        if (currentPage < totalPages - 1) {
            onPageChanged(currentPage + 1)
        }
    }
    
    fun goToPage(page: Int) {
        val totalPages = _uiState.value.totalPages
        if (page >= 0 && page < totalPages) {
            onPageChanged(page)
        }
    }
    
    fun addBookmark(name: String, note: String? = null) {
        val manga = _uiState.value.manga ?: return
        val currentPage = _uiState.value.currentPage
        
        viewModelScope.launch {
            try {
                val bookmark = Bookmark(
                    mangaId = manga.id,
                    page = currentPage,
                    name = name,
                    note = note
                )
                addBookmarkUseCase(bookmark)
                _uiState.update { it.copy(isBookmarkDialogOpen = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add bookmark") }
            }
        }
    }
    
    fun openBookmarkDialog() {
        _uiState.update { it.copy(isBookmarkDialogOpen = true) }
    }
    
    fun closeBookmarkDialog() {
        _uiState.update { it.copy(isBookmarkDialogOpen = false) }
    }
    
    fun toggleBookmarkDialog() {
        _uiState.update { it.copy(isBookmarkDialogOpen = !it.isBookmarkDialogOpen) }
    }
    
    fun onBookmarkClick(bookmark: Bookmark) {
        goToPage(bookmark.page)
    }
    
    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            // Implement delete bookmark logic
            loadBookmarks(_uiState.value.manga?.id ?: 0L)
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun setReaderDirection(direction: ReaderDirection) {
        _uiState.update { it.copy(readerDirection = direction) }
    }
    
    fun setReaderZoomMode(zoomMode: ZoomMode) {
        _uiState.update { it.copy(zoomMode = zoomMode) }
    }
}

data class ReaderUiState(
    val isLoading: Boolean = true,
    val manga: Manga? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val bookmarks: List<Bookmark> = emptyList(),
    val readingHistory: ReadingHistory? = null,
    val error: String? = null,
    val isBookmarkDialogOpen: Boolean = false,
    val readerDirection: ReaderDirection = ReaderDirection.LEFT_TO_RIGHT,
    val zoomMode: ZoomMode = ZoomMode.FIT_TO_SCREEN
)

enum class ReaderDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

enum class ZoomMode {
    FIT_TO_SCREEN,
    FILL_SCREEN,
    ORIGINAL_SIZE
}