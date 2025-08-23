package com.easycomic.ui_reader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

/**
 * 阅读器ViewModel
 * 负责管理阅读状态、页面加载和进度保存
 * 实现300ms防抖机制优化性能
 */
class ReaderViewModel(
    private val getMangaByIdUseCase: GetMangaByIdUseCase,
    private val updateReadingProgressUseCase: UpdateReadingProgressUseCase,
    private val comicParserFactory: ComicParserFactory
) : ViewModel() {

    // mangaId可以通过setMangaId动态设置
    private var mangaId: Long = 0L

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var comicParser: ComicParser? = null
    private var saveProgressJob: Job? = null
    
    // 改进的图片缓存系统
    private val imageCache = mutableMapOf<Int, CachedBitmap>()
    private val maxCacheSize = 8 // 增加缓存大小到8页
    private val maxMemoryBytes = 50 * 1024 * 1024L // 50MB内存限制
    private var currentMemoryUsage = 0L
    
    /**
     * 缓存的位图数据
     */
    private data class CachedBitmap(
        val bitmap: Bitmap,
        val lastAccessed: Long,
        val sizeBytes: Long
    )
    
    companion object {
        private const val SAVE_PROGRESS_DEBOUNCE_MS = 300L // 300ms防抖
        private const val MENU_AUTO_HIDE_DELAY_MS = 3000L // 3秒后自动隐藏菜单
    }
    
    /**
     * 设置要阅读的漫画ID并开始加载
     */
    fun setMangaId(newMangaId: Long) {
        if (mangaId != newMangaId) {
            mangaId = newMangaId
            clearCache()
            loadManga()
        }
    }

    private fun loadManga() {
        if (mangaId <= 0L) {
            _uiState.update { it.copy(isLoading = false, error = "请选择要阅读的漫画") }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val manga = getMangaByIdUseCase(mangaId)
                if (manga == null) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "漫画不存在或已被删除") 
                    }
                    return@launch
                }

                val file = File(manga.filePath)
                if (!file.exists()) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "文件不存在: ${manga.filePath}") 
                    }
                    return@launch
                }

                comicParser = comicParserFactory.create(file)
                if (comicParser == null) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "不支持的文件格式") 
                    }
                    return@launch
                }

                val pageCount = comicParser?.getPageCount() ?: 0
                if (pageCount == 0) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "文件中没有找到有效的图片") 
                    }
                    return@launch
                }

                val currentPage = manga.currentPage.coerceIn(0, pageCount - 1)
                
                _uiState.update {
                    it.copy(
                        manga = manga,
                        pageCount = pageCount,
                        currentPage = currentPage,
                        isLoading = false,
                        readingProgress = if (pageCount > 0) currentPage.toFloat() / pageCount else 0f
                    )
                }
                
                // 预加载当前页和下一页
                preloadPages(currentPage)
                
            } catch (e: Exception) {
                Timber.e(e, "加载漫画失败: mangaId=$mangaId")
                _uiState.update { 
                    it.copy(isLoading = false, error = "加载漫画失败: ${e.message}") 
                }
            }
        }
    }

    /**
     * 智能预加载页面，根据内存使用情况调整策略
     */
    private fun preloadPages(centerPage: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // 根据可用内存调整预加载范围
            val runtime = Runtime.getRuntime()
            val freeMemory = runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory()
            val preloadRange = when {
                freeMemory > 100 * 1024 * 1024 -> 2 // 100MB以上，预加载前后各2页
                freeMemory > 50 * 1024 * 1024 -> 1  // 50MB以上，预加载前后各1页
                else -> 0 // 内存紧张，不预加载
            }
            
            // 优先加载当前页
            if (!imageCache.containsKey(centerPage)) {
                loadPageToCache(centerPage)
            }
            
            // 然后加载周围页面
            for (distance in 1..preloadRange) {
                val prevPage = centerPage - distance
                val nextPage = centerPage + distance
                
                if (prevPage >= 0 && !imageCache.containsKey(prevPage)) {
                    loadPageToCache(prevPage)
                }
                if (nextPage < _uiState.value.pageCount && !imageCache.containsKey(nextPage)) {
                    loadPageToCache(nextPage)
                }
            }
            
            // 清理过期缓存
            cleanupCache(centerPage)
        }
    }
    
    private suspend fun loadPageToCache(pageIndex: Int) {
        try {
            // 检查内存限制
            if (currentMemoryUsage > maxMemoryBytes) {
                cleanupOldestCaches()
            }
            
            val bitmap = comicParser?.getPageStream(pageIndex)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            
            if (bitmap != null) {
                val sizeBytes = bitmap.byteCount.toLong()
                imageCache[pageIndex] = CachedBitmap(
                    bitmap = bitmap,
                    lastAccessed = System.currentTimeMillis(),
                    sizeBytes = sizeBytes
                )
                currentMemoryUsage += sizeBytes
                
                Timber.d("缓存页面 $pageIndex, 大小: ${sizeBytes / 1024}KB, 总内存使用: ${currentMemoryUsage / 1024 / 1024}MB")
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to preload page $pageIndex")
        }
    }
    
    /**
     * LRU缓存清理策略
     */
    private fun cleanupCache(centerPage: Int) {
        val keysToRemove = mutableListOf<Int>()
        
        // 按距离和最后访问时间决定清理顺序
        imageCache.entries.sortedWith { a, b ->
            val distanceA = kotlin.math.abs(a.key - centerPage)
            val distanceB = kotlin.math.abs(b.key - centerPage)
            
            when {
                distanceA != distanceB -> distanceA.compareTo(distanceB)
                else -> a.value.lastAccessed.compareTo(b.value.lastAccessed)
            }
        }.let { sortedEntries ->
            // 保留距离中心页面最近的页面
            sortedEntries.drop(maxCacheSize).forEach { entry ->
                keysToRemove.add(entry.key)
            }
        }
        
        // 执行清理
        keysToRemove.forEach { key ->
            imageCache.remove(key)?.let { cached ->
                cached.bitmap.recycle()
                currentMemoryUsage -= cached.sizeBytes
            }
        }
        
        if (keysToRemove.isNotEmpty()) {
            Timber.d("清理缓存 ${keysToRemove.size} 页，剩余内存使用: ${currentMemoryUsage / 1024 / 1024}MB")
        }
    }
    
    /**
     * 清理最旧的缓存以释放内存
     */
    private fun cleanupOldestCaches() {
        val oldestEntries = imageCache.entries
            .sortedBy { it.value.lastAccessed }
            .take(maxCacheSize / 2) // 清理一半最旧的缓存
        
        oldestEntries.forEach { entry ->
            imageCache.remove(entry.key)?.let { cached ->
                cached.bitmap.recycle()
                currentMemoryUsage -= cached.sizeBytes
            }
        }
        
        Timber.d("内存清理完成，释放 ${oldestEntries.size} 页缓存")
    }

    fun nextPage() {
        val currentPage = _uiState.value.currentPage
        val pageCount = _uiState.value.pageCount
        
        if (currentPage < pageCount - 1) {
            val nextPage = currentPage + 1
            goToPage(nextPage)
        }
    }

    fun previousPage() {
        val currentPage = _uiState.value.currentPage
        
        if (currentPage > 0) {
            val prevPage = currentPage - 1
            goToPage(prevPage)
        }
    }

    fun goToPage(pageIndex: Int) {
        val pageCount = _uiState.value.pageCount
        val validPageIndex = pageIndex.coerceIn(0, pageCount - 1)
        
        if (validPageIndex != _uiState.value.currentPage) {
            _uiState.update { currentState ->
                currentState.copy(
                    currentPage = validPageIndex,
                    readingProgress = if (pageCount > 0) validPageIndex.toFloat() / pageCount else 0f
                )
            }
            
            // 预加载周围页面
            preloadPages(validPageIndex)
            
            // 防抖保存进度
            debouncedSaveProgress()
        }
    }

    /**
     * 防抖保存阅读进度，避免频繁数据库操作
     */
    private fun debouncedSaveProgress() {
        saveProgressJob?.cancel()
        saveProgressJob = viewModelScope.launch {
            delay(SAVE_PROGRESS_DEBOUNCE_MS)
            saveProgressInternal()
        }
    }
    
    private suspend fun saveProgressInternal() {
        try {
            val currentState = _uiState.value
            val manga = currentState.manga ?: return
            
            updateReadingProgressUseCase(
                mangaId = manga.id,
                currentPage = currentState.currentPage,
                status = com.easycomic.domain.model.ReadingStatus.READING
            )
            
            Timber.d("Progress saved: page ${currentState.currentPage}/${currentState.pageCount}")
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to save reading progress")
        }
    }

    fun toggleMenu() {
        _uiState.update { currentState ->
            currentState.copy(
                settings = currentState.settings.copy(
                    isMenuVisible = !currentState.settings.isMenuVisible
                )
            )
        }
        
        // 如果菜单变为可见，设置自动隐藏
        if (_uiState.value.settings.isMenuVisible) {
            scheduleMenuAutoHide()
        }
    }
    
    private fun scheduleMenuAutoHide() {
        viewModelScope.launch {
            delay(MENU_AUTO_HIDE_DELAY_MS)
            _uiState.update { currentState ->
                currentState.copy(
                    settings = currentState.settings.copy(isMenuVisible = false)
                )
            }
        }
    }

    fun setReadingMode(mode: ReadingMode) {
        _uiState.update { currentState ->
            currentState.copy(
                settings = currentState.settings.copy(readingMode = mode)
            )
        }
    }

    fun setReadingDirection(direction: ReadingDirection) {
        _uiState.update { currentState ->
            currentState.copy(
                settings = currentState.settings.copy(readingDirection = direction)
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 获取页面图片，优先从缓存获取
     */
    suspend fun getPageBitmap(pageIndex: Int): Bitmap? {
        // 先从缓存获取
        imageCache[pageIndex]?.let { cached ->
            // 更新访问时间
            imageCache[pageIndex] = cached.copy(lastAccessed = System.currentTimeMillis())
            return cached.bitmap
        }
        
        // 缓存中没有，直接加载
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = comicParser?.getPageStream(pageIndex)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                
                // 加载成功后放入缓存
                if (bitmap != null) {
                    val sizeBytes = bitmap.byteCount.toLong()
                    
                    // 检查内存限制
                    if (currentMemoryUsage + sizeBytes > maxMemoryBytes) {
                        cleanupOldestCaches()
                    }
                    
                    imageCache[pageIndex] = CachedBitmap(
                        bitmap = bitmap,
                        lastAccessed = System.currentTimeMillis(),
                        sizeBytes = sizeBytes
                    )
                    currentMemoryUsage += sizeBytes
                }
                
                bitmap
            } catch (e: Exception) {
                Timber.e(e, "Failed to load bitmap for page $pageIndex")
                null
            }
        }
    }

    /**
     * 清空图片缓存
     */
    private fun clearCache() {
        imageCache.values.forEach { cached ->
            if (!cached.bitmap.isRecycled) {
                cached.bitmap.recycle()
            }
        }
        imageCache.clear()
        currentMemoryUsage = 0L
    }

    override fun onCleared() {
        super.onCleared()
        
        // 立即保存当前进度
        saveProgressJob?.cancel()
        viewModelScope.launch {
            saveProgressInternal()
        }
        
        // 清理资源
        viewModelScope.launch(Dispatchers.IO) {
            imageCache.values.forEach { cached ->
                cached.bitmap.recycle()
            }
            imageCache.clear()
            currentMemoryUsage = 0L
            
            try {
                comicParser?.close()
            } catch (e: Exception) {
                Timber.e(e, "Failed to close comic parser")
            }
        }
    }
}
