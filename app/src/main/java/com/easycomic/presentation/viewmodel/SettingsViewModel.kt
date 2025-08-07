package com.easycomic.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.core.database.Manga
import com.easycomic.domain.usecase.manga.GetMangaListUseCase
import com.easycomic.domain.usecase.manga.UpdateMangaUseCase
import com.easycomic.domain.usecase.webdav.SyncWebDavUseCase
import com.easycomic.domain.usecase.webdav.TestWebDavConnectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getMangaListUseCase: GetMangaListUseCase,
    private val updateMangaUseCase: UpdateMangaUseCase,
    private val testWebDavConnectionUseCase: TestWebDavConnectionUseCase,
    private val syncWebDavUseCase: SyncWebDavUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun testWebDavConnection(url: String, username: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            
            try {
                val result = testWebDavConnectionUseCase(url, username, password)
                _uiState.update { it.copy(
                    isTestingConnection = false,
                    connectionTestResult = result.isSuccess
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isTestingConnection = false,
                    connectionTestResult = false,
                    error = e.message ?: "Connection test failed"
                ) }
            }
        }
    }
    
    fun syncWithWebDAV() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, syncResult = null) }
            
            try {
                val result = syncWebDavUseCase()
                _uiState.update { it.copy(
                    isSyncing = false,
                    syncResult = result.isSuccess
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isSyncing = false,
                    syncResult = false,
                    error = e.message ?: "Sync failed"
                ) }
            }
        }
    }
    
    fun updateWebDavSettings(url: String, username: String, password: String, enabled: Boolean) {
        _uiState.update { it.copy(
            webDavUrl = url,
            webDavUsername = username,
            webDavPassword = password,
            webDavEnabled = enabled
        ) }
    }
    
    fun updateTheme(theme: AppTheme) {
        _uiState.update { it.copy(theme = theme) }
    }
    
    fun updateReaderDirection(direction: ReaderDirection) {
        _uiState.update { it.copy(readerDirection = direction) }
    }
    
    fun updateDefaultZoomMode(zoomMode: ZoomMode) {
        _uiState.update { it.copy(defaultZoomMode = zoomMode) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun getStatistics() {
        viewModelScope.launch {
            val allManga = getMangaListUseCase().first()
            val favoriteManga = getMangaListUseCase.getFavoriteManga().first()
            val completedManga = getMangaListUseCase.getCompletedManga().first()
            
            _uiState.update { it.copy(
                totalMangaCount = allManga.size,
                favoriteMangaCount = favoriteManga.size,
                completedMangaCount = completedManga.size
            ) }
        }
    }
}

data class SettingsUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val readerDirection: ReaderDirection = ReaderDirection.LEFT_TO_RIGHT,
    val defaultZoomMode: ZoomMode = ZoomMode.FIT_TO_SCREEN,
    val webDavUrl: String = "",
    val webDavUsername: String = "",
    val webDavPassword: String = "",
    val webDavEnabled: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: Boolean? = null,
    val isSyncing: Boolean = false,
    val syncResult: Boolean? = null,
    val totalMangaCount: Int = 0,
    val favoriteMangaCount: Int = 0,
    val completedMangaCount: Int = 0,
    val error: String? = null
)

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ReaderDirection {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
}

enum class ZoomMode {
    FIT_TO_SCREEN,
    FILL_SCREEN,
    ORIGINAL_SIZE
}