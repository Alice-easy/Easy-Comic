package com.easycomic.ui_bookshelf

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.data.repository.ImportProgress
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetAllMangaUseCase
import com.easycomic.domain.usecase.manga.ImportComicsUseCase
import com.easycomic.domain.usecase.manga.DeleteComicsUseCase
import com.easycomic.domain.usecase.manga.UpdateMangaFavoriteStatusUseCase
import com.easycomic.domain.usecase.manga.MarkMangasAsReadUseCase
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
    private val comicImportRepository: ComicImportRepositoryImpl,
    private val deleteComicsUseCase: DeleteComicsUseCase,
    private val updateMangaFavoriteStatusUseCase: UpdateMangaFavoriteStatusUseCase,
    private val markMangasAsReadUseCase: MarkMangasAsReadUseCase
) : ViewModel() {

    private val _comics = MutableStateFlow<List<Manga>>(emptyList())
    private val _allComics = MutableStateFlow<List<Manga>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    private val _isLoading = MutableStateFlow(true)
    private val _importProgress = MutableStateFlow<ImportProgress?>(null)
    private val _isImporting = MutableStateFlow(false)
    private val _selectionMode = MutableStateFlow(false)
    private val _selectedMangas = MutableStateFlow<Set<Long>>(emptySet())
    
    open fun getComics(): StateFlow<List<Manga>> = _comics.asStateFlow()
    
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val importProgress: StateFlow<ImportProgress?> = _importProgress.asStateFlow()
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()
    val selectedMangas: StateFlow<Set<Long>> = _selectedMangas.asStateFlow()

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
    
    // === 批量操作和选择模式相关方法 ===
    
    /**
     * 进入选择模式并选择第一个漫画
     */
    fun enterSelectionMode(mangaId: Long) {
        _selectionMode.value = true
        _selectedMangas.value = setOf(mangaId)
    }
    
    /**
     * 退出选择模式
     */
    fun clearSelection() {
        _selectionMode.value = false
        _selectedMangas.value = emptySet()
    }
    
    /**
     * 切换漫画的选择状态
     */
    fun toggleMangaSelection(mangaId: Long) {
        val currentSelected = _selectedMangas.value
        _selectedMangas.value = if (currentSelected.contains(mangaId)) {
            currentSelected - mangaId
        } else {
            currentSelected + mangaId
        }
        
        // 如果没有选中任何项，退出选择模式
        if (_selectedMangas.value.isEmpty()) {
            _selectionMode.value = false
        }
    }
    
    /**
     * 全选当前可见的漫画
     */
    fun selectAllVisibleMangas(visibleMangas: List<Manga>) {
        _selectedMangas.value = visibleMangas.map { it.id }.toSet()
    }
    
    /**
     * 删除选中的漫画
     */
    fun deleteSelectedMangas() {
        viewModelScope.launch {
            try {
                val selectedIds = _selectedMangas.value.toList()
                deleteComicsUseCase.deleteMultiple(selectedIds)
                Timber.d("成功删除漫画: $selectedIds")
                
                // 清除选择状态
                clearSelection()
                
                // 重新加载数据
                loadComics()
            } catch (e: Exception) {
                Timber.e(e, "删除漫画失败")
            }
        }
    }
    
    /**
     * 标记选中的漫画为收藏/取消收藏
     */
    fun markSelectedAsFavorite(isFavorite: Boolean) {
        viewModelScope.launch {
            try {
                val selectedIds = _selectedMangas.value.toList()
                updateMangaFavoriteStatusUseCase.updateMultiple(selectedIds, isFavorite)
                Timber.d("成功更新收藏状态: $selectedIds -> $isFavorite")
                
                // 清除选择状态
                clearSelection()
                
                // 重新加载数据
                loadComics()
            } catch (e: Exception) {
                Timber.e(e, "更新收藏状态失败")
            }
        }
    }
    
    /**
     * 标记选中的漫画为已读
     */
    fun markSelectedAsRead() {
        viewModelScope.launch {
            try {
                val selectedIds = _selectedMangas.value.toList()
                markMangasAsReadUseCase.markMultipleAsRead(selectedIds)
                Timber.d("成功标记为已读: $selectedIds")
                
                // 清除选择状态
                clearSelection()
                
                // 重新加载数据
                loadComics()
            } catch (e: Exception) {
                Timber.e(e, "标记为已读失败")
            }
        }
    }
}