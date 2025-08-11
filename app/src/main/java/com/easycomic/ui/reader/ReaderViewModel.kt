package com.easycomic.ui.reader

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
import com.easycomic.image.ImageLoader
import com.easycomic.model.Comic
import com.easycomic.model.ComicPage
import com.easycomic.model.ParseResult
import com.easycomic.parser.ComicParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * 阅读器ViewModel - 使用 Clean Architecture
 */
class ReaderViewModel(
    private val getMangaByIdUseCase: GetMangaByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase
) : ViewModel() {
    
    private val comicParser = ComicParser()
    private val imageLoader = ImageLoader()
    
    // UI状态
    private val _uiState = mutableStateOf(ReaderUiState())
    val uiState = _uiState
    
    // 当前的漫画和页面
    private var currentComic: Comic? = null
    private var currentPageList: List<ComicPage> = emptyList()
    
    // 进度保存任务
    private var progressSaveJob: Job? = null
    
    init {
        Timber.d("ReaderViewModel initialized")
    }
    
    /**
     * 加载漫画
     */
    fun loadComic(filePath: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                comicParser.parseComicFile(filePath).collect { result ->
                    when (result) {
                        is ParseResult.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                progress = result.progress
                            )
                        }
                        is ParseResult.Success -> {
                            currentComic = result.comic
                            currentPageList = result.pages
                            
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                progress = 1f,
                                currentPage = result.comic.currentPage,
                                maxPage = result.pages.size - 1,
                                comicTitle = result.comic.title
                            )
                            
                            // 加载当前页面
                            loadCurrentPage()
                        }
                        is ParseResult.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "加载漫画失败")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "加载漫画失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 加载当前页面
     */
    private fun loadCurrentPage() {
        viewModelScope.launch {
            val currentPageIndex = _uiState.value.currentPage
            if (currentPageIndex in currentPageList.indices) {
                val page = currentPageList[currentPageIndex]
                if (page.imageData != null) {
                    imageLoader.loadImage(
                        imageData = page.imageData,
                        reqWidth = _uiState.value.screenWidth,
                        reqHeight = _uiState.value.screenHeight
                    ).fold(
                        onSuccess = { bitmap ->
                            _uiState.value = _uiState.value.copy(
                                currentPageBitmap = bitmap,
                                isLoadingImage = false
                            )
                            
                            // 预加载下一页
                            prefetchNextPages(currentPageIndex)
                        },
                        onFailure = { exception ->
                            Timber.e(exception, "加载页面图像失败")
                            _uiState.value = _uiState.value.copy(
                                error = "加载页面图像失败",
                                isLoadingImage = false
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "页面图像数据为空",
                        isLoadingImage = false
                    )
                }
            }
        }
    }
    
    /**
     * 导航到下一页
     */
    fun nextPage() {
        val currentPage = _uiState.value.currentPage
        val maxPage = _uiState.value.maxPage
        
        if (currentPage < maxPage) {
            _uiState.value = _uiState.value.copy(
                currentPage = currentPage + 1,
                isLoadingImage = true
            )
            loadCurrentPage()
            saveProgress(currentPage + 1)
        }
    }
    
    /**
     * 导航到上一页
     */
    fun previousPage() {
        val currentPage = _uiState.value.currentPage
        
        if (currentPage > 0) {
            _uiState.value = _uiState.value.copy(
                currentPage = currentPage - 1,
                isLoadingImage = true
            )
            loadCurrentPage()
            saveProgress(currentPage - 1)
        }
    }
    
    /**
     * 跳转到指定页面
     */
    fun goToPage(pageNumber: Int) {
        val maxPage = _uiState.value.maxPage
        
        if (pageNumber in 0..maxPage) {
            _uiState.value = _uiState.value.copy(
                currentPage = pageNumber,
                isLoadingImage = true
            )
            loadCurrentPage()
            saveProgress(pageNumber)
        }
    }
    
    /**
     * 保存阅读进度（带防抖）
     */
    private fun saveProgress(pageNumber: Int) {
        progressSaveJob?.cancel()
        
        progressSaveJob = viewModelScope.launch {
            delay(300) // 防抖300ms
            
            currentComic?.let { comic ->
                // 这里可以保存到SharedPreferences或数据库
                Timber.d("保存进度: ${comic.title} - 页面 $pageNumber")
            }
        }
    }
    
    /**
     * 预加载下一页
     */
    private fun prefetchNextPages(currentPageIndex: Int) {
        viewModelScope.launch {
            // 预加载接下来的2页
            for (i in 1..2) {
                val targetIndex = currentPageIndex + i
                if (targetIndex in currentPageList.indices) {
                    val page = currentPageList[targetIndex]
                    if (page.imageData != null) {
                        imageLoader.loadImage(
                            imageData = page.imageData,
                            reqWidth = _uiState.value.screenWidth,
                            reqHeight = _uiState.value.screenHeight
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 设置屏幕尺寸
     */
    fun setScreenSize(width: Int, height: Int) {
        _uiState.value = _uiState.value.copy(
            screenWidth = width,
            screenHeight = height
        )
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * 获取当前进度
     */
    fun getCurrentProgress(): Float {
        val currentPage = _uiState.value.currentPage
        val maxPage = _uiState.value.maxPage
        return if (maxPage > 0) (currentPage.toFloat() / maxPage.toFloat()) else 0f
    }
    
    /**
     * 强制保存进度
     */
    fun forceSaveProgress() {
        progressSaveJob?.cancel()
        saveProgress(_uiState.value.currentPage)
    }
    
    /**
     * 获取内存信息
     */
    fun getMemoryInfo(): String {
        return imageLoader.getMemoryInfo()
    }
    
    /**
     * 获取缓存信息
     */
    fun getCacheInfo(): String {
        return imageLoader.getCacheInfo()
    }
    
    override fun onCleared() {
        super.onCleared()
        imageLoader.clearCache()
        Timber.d("ReaderViewModel cleared")
    }
}

/**
 * 阅读器UI状态
 */
data class ReaderUiState(
    val isLoading: Boolean = false,
    val isLoadingImage: Boolean = false,
    val progress: Float = 0f,
    val currentPage: Int = 0,
    val maxPage: Int = 0,
    val comicTitle: String = "",
    val currentPageBitmap: android.graphics.Bitmap? = null,
    val error: String? = null,
    val screenWidth: Int = 1080,
    val screenHeight: Int = 1920
)