package com.easycomic.ui_bookshelf

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.data.repository.ImportProgress
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
    private val importComicsUseCase: ImportComicsUseCase,
    private val comicImportRepository: ComicImportRepositoryImpl
) : ViewModel() {

    private val _comics = MutableStateFlow<List<Manga>>(emptyList())
    private val _allComics = MutableStateFlow<List<Manga>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    private val _isLoading = MutableStateFlow(true)
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    private val _isImporting = MutableStateFlow(false)
    
    open fun getComics(): StateFlow<List<Manga>> = _comics.asStateFlow()
    
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    enum class SortOrder {
        TITLE_ASC, TITLE_DESC,
        DATE_ADDED_ASC, DATE_ADDED_DESC,
        LAST_READ_ASC, LAST_READ_DESC,
        PROGRESS_ASC, PROGRESS_DESC
    }

    init {
        loadComics()
    }

    private fun loadComics() {
        viewModelScope.launch {
            _isLoading.value = true
            getAllMangaUseCase()
                .catch { e ->
                    Timber.e(e, "Failed to load comics")
                    _isLoading.value = false
                }
                .collect { mangaList ->
                    _allComics.value = mangaList
                    _isLoading.value = false
                    applyFiltersAndSort()
                }
        }
    }

    fun searchComics(query: String) {
        _searchQuery.value = query
        applyFiltersAndSort()
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        viewModelScope.launch {
            var filteredComics = _allComics.value
            
            // 应用搜索过滤
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                filteredComics = filteredComics.filter { manga ->
                    manga.title.contains(query, ignoreCase = true) ||
                    manga.author?.contains(query, ignoreCase = true) == true
                }
            }
            
            // 应用排序
            filteredComics = when (_sortOrder.value) {
                SortOrder.TITLE_ASC -> filteredComics.sortedBy { it.title }
                SortOrder.TITLE_DESC -> filteredComics.sortedByDescending { it.title }
                SortOrder.DATE_ADDED_ASC -> filteredComics.sortedBy { it.dateAdded }
                SortOrder.DATE_ADDED_DESC -> filteredComics.sortedByDescending { it.dateAdded }
                SortOrder.LAST_READ_ASC -> filteredComics.sortedBy { manga: Manga -> manga.lastRead ?: 0 }
                SortOrder.LAST_READ_DESC -> filteredComics.sortedByDescending { manga: Manga -> manga.lastRead ?: 0 }
                SortOrder.PROGRESS_ASC -> filteredComics.sortedBy { manga: Manga -> if (manga.pageCount > 0) manga.currentPage.toFloat() / manga.pageCount.toFloat() else 0f }
                SortOrder.PROGRESS_DESC -> filteredComics.sortedByDescending { manga: Manga -> if (manga.pageCount > 0) manga.currentPage.toFloat() / manga.pageCount.toFloat() else 0f }
            }
            
            _comics.value = filteredComics
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
    
    /**
     * 从Document Tree URI导入漫画（支持SAF）
     */
    fun importFromDocumentTree(treeUri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            _importProgress.value = ImportProgress.Scanning
            
            try {
                comicImportRepository.importFromDocumentTree(treeUri)
                    .catch { e ->
                        Timber.e(e, "Failed to import from document tree")
                        _importProgress.value = ImportProgress.Error(e.message ?: "导入失败")
                    }
                    .collect { progress ->
                        _importProgress.value = progress
                        
                        if (progress is ImportProgress.Completed || progress is ImportProgress.Error) {
                            _isImporting.value = false
                            if (progress is ImportProgress.Completed) {
                                // 重新加载漫画列表
                                loadComics()
                            }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start import process")
                _importProgress.value = ImportProgress.Error(e.message ?: "导入失败")
                _isImporting.value = false
            }
        }
    }
    
    /**
     * 清除导入进度状态
     */
    fun clearImportProgress() {
        _importProgress.value = null
    }
    
    /**
     * 刷新漫画列表
     */
    fun refreshComics() {
        loadComics()
    }
}