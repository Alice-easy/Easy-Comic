package com.easycomic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.core.database.Manga
import com.easycomic.domain.usecase.manga.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookshelfViewModel @Inject constructor(
    private val getMangaListUseCase: GetMangaListUseCase,
    private val getMangaUseCase: GetMangaUseCase,
    private val addMangaUseCase: AddMangaUseCase,
    private val updateMangaUseCase: UpdateMangaUseCase,
    private val deleteMangaUseCase: DeleteMangaUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookshelfUiState())
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()
    
    init {
        loadMangaList()
    }
    
    private fun loadMangaList() {
        viewModelScope.launch {
            getMangaListUseCase()
                .catch { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error"
                    ) }
                }
                .collect { mangaList ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        mangaList = mangaList,
                        error = null
                    ) }
                }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            
            if (query.isEmpty()) {
                loadMangaList()
            } else {
                getMangaListUseCase.searchManga(query)
                    .catch { error ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = error.message ?: "Search error"
                        ) }
                    }
                    .collect { mangaList ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            mangaList = mangaList,
                            error = null
                        ) }
                    }
            }
        }
    }
    
    fun onMangaClick(manga: Manga) {
        _uiState.update { it.copy(selectedManga = manga) }
    }
    
    fun onFavoriteClick(mangaId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            updateMangaUseCase.updateFavoriteStatus(mangaId, isFavorite)
        }
    }
    
    fun onDeleteManga(manga: Manga) {
        viewModelScope.launch {
            deleteMangaUseCase(manga)
            loadMangaList()
        }
    }
    
    fun addNewManga(manga: Manga) {
        viewModelScope.launch {
            try {
                addMangaUseCase(manga)
                loadMangaList()
                _uiState.update { it.copy(isAddDialogOpen = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to add manga") }
            }
        }
    }
    
    fun openAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = true) }
    }
    
    fun closeAddDialog() {
        _uiState.update { it.copy(isAddDialogOpen = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class BookshelfUiState(
    val isLoading: Boolean = true,
    val mangaList: List<Manga> = emptyList(),
    val searchQuery: String = "",
    val selectedManga: Manga? = null,
    val error: String? = null,
    val isAddDialogOpen: Boolean = false
)