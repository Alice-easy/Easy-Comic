package com.easycomic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.core.database.Manga
import com.easycomic.domain.usecase.manga.GetMangaListUseCase
import com.easycomic.domain.usecase.manga.UpdateMangaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getMangaListUseCase: GetMangaListUseCase,
    private val updateMangaUseCase: UpdateMangaUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()
    
    init {
        loadFavorites()
    }
    
    private fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            getMangaListUseCase.getFavoriteManga()
                .catch { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load favorites"
                    ) }
                }
                .collect { favorites ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        favoriteManga = favorites,
                        error = null
                    ) }
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
    
    fun onRemoveFavorite(mangaId: Long) {
        viewModelScope.launch {
            updateMangaUseCase.updateFavoriteStatus(mangaId, false)
            // The flow will automatically update the list
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(searchQuery = query) }
            
            if (query.isEmpty()) {
                loadFavorites()
            } else {
                getMangaListUseCase.searchManga(query)
                    .map { mangaList -> mangaList.filter { it.isFavorite } }
                    .catch { error ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = error.message ?: "Search error"
                        ) }
                    }
                    .collect { filteredFavorites ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            favoriteManga = filteredFavorites,
                            error = null
                        ) }
                    }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun sortFavorites(sortBy: SortBy) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favoriteManga
            val sortedFavorites = when (sortBy) {
                SortBy.TITLE -> currentFavorites.sortedBy { it.title }
                SortBy.DATE_ADDED -> currentFavorites.sortedByDescending { it.dateAdded }
                SortBy.LAST_READ -> currentFavorites.sortedByDescending { it.lastModified }
                SortBy.RATING -> currentFavorites.sortedByDescending { it.rating }
            }
            _uiState.update { it.copy(favoriteManga = sortedFavorites, currentSortBy = sortBy) }
        }
    }
}

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val favoriteManga: List<Manga> = emptyList(),
    val searchQuery: String = "",
    val selectedManga: Manga? = null,
    val currentSortBy: SortBy = SortBy.DATE_ADDED,
    val error: String? = null
)

enum class SortBy {
    TITLE,
    DATE_ADDED,
    LAST_READ,
    RATING
}