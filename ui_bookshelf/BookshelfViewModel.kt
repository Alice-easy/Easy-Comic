package com.easycomic.ui.bookshelf

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.*
import com.easycomic.data.entity.ReadingStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

/**
 * 书架视图模型
 */
class BookshelfViewModel(
    private val getAllMangaUseCase: GetAllMangaUseCase,
    private val searchMangaUseCase: SearchMangaUseCase,
    private val getFavoriteMangaUseCase: GetFavoriteMangaUseCase,
    private val getRecentMangaUseCase: GetRecentMangaUseCase,
    private val deleteMangaUseCase: DeleteMangaUseCase,
    private val deleteAllMangaUseCase: DeleteAllMangaUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val importComicsUseCase: ImportComicsUseCase
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(BookshelfUiState())
    val uiState: StateFlow<BookshelfUiState> = _uiState.asStateFlow()
    
    // 搜索状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 排序状态
    private val _sortOption = MutableStateFlow(SortOption.DATE_ADDED_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    // 筛选状态
    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption: StateFlow<FilterOption> = _filterOption.asStateFlow()
    
    // 多选状态
    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode.asStateFlow()
    
    // 已选择的漫画ID
    private val _selectedMangaIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedMangaIds: StateFlow<Set<Long>> = _selectedMangaIds.asStateFlow()
    
    init {
        // 加载所有漫画
        loadAllManga()
        
        // 监听搜索、排序、筛选状态变化
        combine(
            searchQuery,
            sortOption,
            filterOption
        ) { query, sort, filter ->
            applyFiltersAndSort(query, sort, filter)
        }.launchIn(viewModelScope)
    }
    
    /**
     * 加载所有漫画
     */
    private fun loadAllManga() {
        viewModelScope.launch {
            getAllMangaUseCase()
                .catch { e ->
                    Timber.e(e, "加载漫画列表失败")
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "加载漫画列表失败: ${e.message}"
                    ) }
                }
                .collect { mangaList ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        mangaList = mangaList,
                        filteredMangaList = mangaList
                    ) }
                    
                    // 应用筛选和排序
                    applyFiltersAndSort(searchQuery.value, sortOption.value, filterOption.value)
                }
        }
    }
    
    /**
     * 搜索漫画
     */
    fun searchManga(query: String) {
        _searchQuery.value = query
        
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchMangaUseCase(query)
                    .catch { e ->
                        Timber.e(e, "搜索漫画失败")
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "搜索漫画失败: ${e.message}"
                        ) }
                    }
                    .collect { mangaList ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            mangaList = mangaList,
                            filteredMangaList = mangaList
                        ) }
                        
                        // 应用筛选和排序
                        applyFiltersAndSort(query, sortOption.value, filterOption.value)
                    }
            }
        } else {
            loadAllManga()
        }
    }
    
    /**
     * 设置排序选项
     */
    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }
    
    /**
     * 设置筛选选项
     */
    fun setFilterOption(filterOption: FilterOption) {
        _filterOption.value = filterOption
    }
    
    /**
     * 应用筛选和排序
     */
    private fun applyFiltersAndSort(query: String, sort: SortOption, filter: FilterOption) {
        val currentList = _uiState.value.mangaList
        
        // 应用筛选
        val filteredList = when (filter) {
            FilterOption.ALL -> currentList
            FilterOption.FAVORITES -> currentList.filter { it.isFavorite }
            FilterOption.READING -> currentList.filter { it.readingStatus == ReadingStatus.READING }
            FilterOption.COMPLETED -> currentList.filter { it.readingStatus == ReadingStatus.COMPLETED }
            FilterOption.UNREAD -> currentList.filter { it.readingStatus == ReadingStatus.UNREAD }
        }
        
        // 应用排序
        val sortedList = when (sort) {
            SortOption.TITLE_ASC -> filteredList.sortedBy { it.title }
            SortOption.TITLE_DESC -> filteredList.sortedByDescending { it.title }
            SortOption.DATE_ADDED_ASC -> filteredList.sortedBy { it.dateAdded }
            SortOption.DATE_ADDED_DESC -> filteredList.sortedByDescending { it.dateAdded }
            SortOption.LAST_READ_ASC -> filteredList.sortedBy { it.lastRead }
            SortOption.LAST_READ_DESC -> filteredList.sortedByDescending { it.lastRead }
            SortOption.RATING_ASC -> filteredList.sortedBy { it.rating }
            SortOption.RATING_DESC -> filteredList.sortedByDescending { it.rating }
        }
        
        _uiState.update { it.copy(filteredMangaList = sortedList) }
    }
    
    /**
     * 切换选择模式
     */
    fun toggleSelectionMode() {
        _selectionMode.value = !_selectionMode.value
        if (!_selectionMode.value) {
            _selectedMangaIds.value = emptySet()
        }
    }
    
    /**
     * 选择/取消选择漫画
     */
    fun toggleMangaSelection(mangaId: Long) {
        _selectedMangaIds.update { current ->
            if (current.contains(mangaId)) {
                current - mangaId
            } else {
                current + mangaId
            }
        }
    }
    
    /**
     * 全选
     */
    fun selectAll() {
        _selectedMangaIds.value = _uiState.value.filteredMangaList.map { it.id }.toSet()
    }
    
    /**
     * 取消全选
     */
    fun deselectAll() {
        _selectedMangaIds.value = emptySet()
    }
    
    /**
     * 删除选中的漫画
     */
    fun deleteSelectedManga() {
        viewModelScope.launch {
            val selectedManga = _uiState.value.filteredMangaList
                .filter { it.id in _selectedMangaIds.value }
            
            if (selectedManga.isNotEmpty()) {
                deleteAllMangaUseCase(selectedManga)
                _selectedMangaIds.value = emptySet()
                _selectionMode.value = false
                // 重新加载数据
                loadAllManga()
            }
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite(mangaId: Long) {
        viewModelScope.launch {
            toggleFavoriteUseCase(mangaId)
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 导入漫画
     */
    fun importComics(directory: File) {
        viewModelScope.launch {
            try {
                importComicsUseCase(directory)
                // Optionally, you can refresh the manga list after import
                loadAllManga()
            } catch (e: Exception) {
                Timber.e(e, "导入漫画失败")
                _uiState.update { it.copy(error = "导入漫画失败: ${e.message}") }
            }
        }
    }
}

/**
 * 书架UI状态
 */
data class BookshelfUiState(
    val isLoading: Boolean = true,
    val mangaList: List<Manga> = emptyList(),
    val filteredMangaList: List<Manga> = emptyList(),
    val error: String? = null
)

/**
 * 排序选项
 */
enum class SortOption {
    TITLE_ASC,       // 标题升序
    TITLE_DESC,      // 标题降序
    DATE_ADDED_ASC,  // 添加时间升序
    DATE_ADDED_DESC, // 添加时间降序
    LAST_READ_ASC,   // 最后阅读升序
    LAST_READ_DESC,  // 最后阅读降序
    RATING_ASC,      // 评分升序
    RATING_DESC      // 评分降序
}

/**
 * 筛选选项
 */
enum class FilterOption {
    ALL,        // 全部
    FAVORITES,  // 收藏
    READING,    // 阅读中
    COMPLETED,  // 已完成
    UNREAD      // 未读
}