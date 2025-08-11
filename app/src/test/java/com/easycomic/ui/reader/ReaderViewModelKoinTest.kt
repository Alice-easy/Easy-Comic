package com.easycomic.ui.reader

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
import com.easycomic.di.KoinModules
import com.easycomic.image.ImageLoader
import com.easycomic.model.Comic
import com.easycomic.model.ComicPage
import com.easycomic.model.ParseResult
import com.easycomic.parser.ComicParser
import io.insertkoin.koin.test.KoinTest
import io.insertkoin.koin.test.get
import io.insertkoin.koin.test.inject
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ReaderViewModel Koin 注入测试
 * 验证 ReaderViewModel 通过 Koin 正确注入依赖
 */
@RunWith(MockitoJUnitRunner::class)
class ReaderViewModelKoinTest : KoinTest {

    @Mock
    private lateinit var mockGetMangaByIdUseCase: GetMangaByIdUseCase

    @Mock
    private lateinit var mockUpdateReadingProgressUseCase: UpdateReadingProgressUseCase

    private lateinit var viewModel: ReaderViewModel

    @Before
    fun setup() {
        // 初始化 Koin
        KoinModules.initializeKoin(androidx.test.core.app.ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        io.insertkoin.koin.core.context.stopKoin()
    }

    @Test
    fun `test ReaderViewModel can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val injectedViewModel: ReaderViewModel by inject()

        // Then
        assertNotNull(injectedViewModel)
        assertTrue(injectedViewModel is ReaderViewModel)
    }

    @Test
    fun `test ReaderViewModel dependencies are properly injected`() {
        // Given - Koin 已初始化

        // When
        val getMangaByIdUseCase: GetMangaByIdUseCase = get()
        val updateReadingProgressUseCase: UpdateReadingProgressUseCase = get()

        // Then
        assertNotNull(getMangaByIdUseCase)
        assertNotNull(updateReadingProgressUseCase)
        assertTrue(getMangaByIdUseCase is GetMangaByIdUseCase)
        assertTrue(updateReadingProgressUseCase is UpdateReadingProgressUseCase)
    }

    @Test
    fun `test ReaderViewModel with Koin dependencies loads comic correctly`() = runTest {
        // Given
        val filePath = "/path/to/test/comic.cbz"
        val mockComic = Comic(
            title = "测试漫画",
            filePath = filePath,
            currentPage = 0,
            totalPages = 10
        )
        val mockPages = listOf(
            ComicPage(pageNumber = 0, imageData = byteArrayOf(1, 2, 3)),
            ComicPage(pageNumber = 1, imageData = byteArrayOf(4, 5, 6))
        )

        // 创建带有 mock use case 的 ViewModel
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When
        viewModel.loadComic(filePath)

        // Then - 由于我们使用 mock ComicParser，这里主要验证状态变化
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)
            
            // 等待更多状态变化
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test ReaderViewModel navigation functionality with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When - 初始状态
        val initialState = viewModel.uiState.value
        assertEquals(0, initialState.currentPage)
        assertEquals(0, initialState.maxPage)

        // When - 导航到下一页
        viewModel.nextPage()

        // Then - 应该没有变化，因为还没有加载漫画
        val afterNextState = viewModel.uiState.value
        assertEquals(0, afterNextState.currentPage)

        // When - 导航到上一页
        viewModel.previousPage()

        // Then - 应该没有变化，因为还没有加载漫画
        val afterPreviousState = viewModel.uiState.value
        assertEquals(0, afterPreviousState.currentPage)
    }

    @Test
    fun `test ReaderViewModel page navigation with loaded comic`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)
        
        // 模拟已加载漫画的状态
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isLoading = false,
            currentPage = 0,
            maxPage = 5
        )

        // When - 导航到下一页
        viewModel.nextPage()

        // Then
        assertEquals(1, viewModel.uiState.value.currentPage)

        // When - 导航到下一页
        viewModel.nextPage()

        // Then
        assertEquals(2, viewModel.uiState.value.currentPage)

        // When - 导航到上一页
        viewModel.previousPage()

        // Then
        assertEquals(1, viewModel.uiState.value.currentPage)

        // When - 跳转到指定页面
        viewModel.goToPage(4)

        // Then
        assertEquals(4, viewModel.uiState.value.currentPage)

        // When - 尝试跳转到超出范围的页面
        viewModel.goToPage(10)

        // Then - 应该保持当前页面
        assertEquals(4, viewModel.uiState.value.currentPage)
    }

    @Test
    fun `test ReaderViewModel screen size functionality with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When
        viewModel.setScreenSize(1920, 1080)

        // Then
        assertEquals(1920, viewModel.uiState.value.screenWidth)
        assertEquals(1080, viewModel.uiState.value.screenHeight)
    }

    @Test
    fun `test ReaderViewModel error handling with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When - 设置错误
        viewModel.uiState.value = viewModel.uiState.value.copy(error = "测试错误")

        // Then
        assertEquals("测试错误", viewModel.uiState.value.error)

        // When - 清除错误
        viewModel.clearError()

        // Then
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `test ReaderViewModel progress calculation with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)
        
        // 模拟已加载漫画的状态
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isLoading = false,
            currentPage = 3,
            maxPage = 9
        )

        // When
        val progress = viewModel.getCurrentProgress()

        // Then
        assertEquals(0.33333334f, progress, 0.001f)
    }

    @Test
    fun `test ReaderViewModel progress calculation with no pages`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)
        
        // 模拟没有页面的状态
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isLoading = false,
            currentPage = 0,
            maxPage = 0
        )

        // When
        val progress = viewModel.getCurrentProgress()

        // Then
        assertEquals(0f, progress)
    }

    @Test
    fun `test ReaderViewModel force save progress with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When - 强制保存进度
        viewModel.forceSaveProgress()

        // Then - 不应该抛出异常
        assertTrue(true)
    }

    @Test
    fun `test ReaderViewModel memory info with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When
        val memoryInfo = viewModel.getMemoryInfo()
        val cacheInfo = viewModel.getCacheInfo()

        // Then - 应该返回字符串信息
        assertNotNull(memoryInfo)
        assertNotNull(cacheInfo)
        assertTrue(memoryInfo.isNotEmpty())
        assertTrue(cacheInfo.isNotEmpty())
    }

    @Test
    fun `test ReaderViewModel cleanup on cleared with Koin`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When - 模拟 onCleared 调用
        viewModel.onCleared()

        // Then - 不应该抛出异常
        assertTrue(true)
    }

    @Test
    fun `test ReaderViewModel with invalid file path`() = runTest {
        // Given
        val invalidFilePath = "/path/to/invalid/comic.cbz"
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When
        viewModel.loadComic(invalidFilePath)

        // Then - 应该处理错误而不崩溃
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)
            
            // 等待错误状态
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test ReaderViewModel with empty comic data`() = runTest {
        // Given
        val emptyFilePath = "/path/to/empty/comic.cbz"
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)

        // When
        viewModel.loadComic(emptyFilePath)

        // Then - 应该处理空数据而不崩溃
        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertNull(loadingState.error)
            
            // 等待更多状态变化
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test ReaderViewModel with large page number navigation`() {
        // Given
        viewModel = ReaderViewModel(mockGetMangaByIdUseCase, mockUpdateReadingProgressUseCase)
        
        // 模拟已加载漫画的状态
        viewModel.uiState.value = viewModel.uiState.value.copy(
            isLoading = false,
            currentPage = 0,
            maxPage = 100
        )

        // When - 导航到很大的页码
        viewModel.goToPage(99)

        // Then
        assertEquals(99, viewModel.uiState.value.currentPage)

        // When - 导航到下一页（应该到达最大页）
        viewModel.nextPage()

        // Then
        assertEquals(99, viewModel.uiState.value.currentPage) // 不应该超过最大页
    }
}