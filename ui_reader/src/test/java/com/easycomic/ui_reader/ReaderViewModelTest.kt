package com.easycomic.ui_reader

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.parser.ComicParser
import com.easycomic.domain.parser.ComicParserFactory
import com.easycomic.domain.usecase.manga.GetMangaByIdUseCase
import com.easycomic.domain.usecase.manga.UpdateReadingProgressUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    // Mock dependencies
    private val mockSavedStateHandle = mockk<SavedStateHandle>()
    private val mockGetMangaByIdUseCase = mockk<GetMangaByIdUseCase>()
    private val mockUpdateReadingProgressUseCase = mockk<UpdateReadingProgressUseCase>()
    private val mockComicParserFactory = mockk<ComicParserFactory>()
    private val mockComicParser = mockk<ComicParser>()
    private val mockFile = mockk<File>()
    private val mockBitmap = mockk<Bitmap>()

    private lateinit var viewModel: ReaderViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testMangaId = 123L
    private val testManga = Manga(
        id = testMangaId,
        title = "Test Manga",
        author = "Test Author",
        filePath = "/path/to/manga.zip",
        totalPages = 100,
        currentPage = 50,
        isFavorite = false,
        readingStatus = ReadingStatus.READING,
        dateAdded = 1000L,
        lastRead = 2000L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Setup SavedStateHandle to return test manga ID
        every { mockSavedStateHandle.get<Long>("mangaId") } returns testMangaId
        
        // Setup default behaviors
        coEvery { mockGetMangaByIdUseCase(testMangaId) } returns testManga
        coEvery { mockUpdateReadingProgressUseCase(any()) } returns Unit
        
        // Setup file and parser mocks
        every { mockFile.exists() } returns true
        every { mockComicParserFactory.create(any()) } returns mockComicParser
        every { mockComicParser.getPageCount() } returns 100
        every { mockComicParser.getPageStream(any()) } returns ByteArrayInputStream(byteArrayOf())
        every { mockComicParser.close() } returns Unit
        
        // Mock bitmap creation (this would normally be handled by BitmapFactory)
        mockkStatic("android.graphics.BitmapFactory")
        every { android.graphics.BitmapFactory.decodeStream(any()) } returns mockBitmap
        every { mockBitmap.byteCount } returns 1024 * 1024 // 1MB
        every { mockBitmap.recycle() } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
        unmockkStatic("android.graphics.BitmapFactory")
    }

    private fun createViewModel(): ReaderViewModel {
        return ReaderViewModel(
            savedStateHandle = mockSavedStateHandle,
            getMangaByIdUseCase = mockGetMangaByIdUseCase,
            updateReadingProgressUseCase = mockUpdateReadingProgressUseCase,
            comicParserFactory = mockComicParserFactory
        )
    }

    // === Initialization Tests ===

    @Test
    fun `should throw exception when mangaId is missing from SavedStateHandle`() = runTest {
        every { mockSavedStateHandle.get<Long>("mangaId") } returns null

        try {
            createViewModel()
            assert(false) { "Expected IllegalArgumentException to be thrown" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).isEqualTo("Missing mangaId")
        }
    }

    @Test
    fun `initial state should load manga successfully`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.manga).isEqualTo(testManga)
            assertThat(state.pageCount).isEqualTo(100)
            assertThat(state.currentPage).isEqualTo(50) // From manga.currentPage
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isNull()
            assertThat(state.readingProgress).isEqualTo(0.5f) // 50/100
        }

        coVerify { mockGetMangaByIdUseCase(testMangaId) }
        verify { mockComicParserFactory.create(any()) }
        verify { mockComicParser.getPageCount() }
    }

    @Test
    fun `should handle manga not found error`() = runTest {
        coEvery { mockGetMangaByIdUseCase(testMangaId) } returns null

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.manga).isNull()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("漫画不存在或已被删除")
        }
    }

    @Test
    fun `should handle file not exists error`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns false

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("文件不存在: /path/to/manga.zip")
        }
    }

    @Test
    fun `should handle unsupported file format error`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { mockComicParserFactory.create(any()) } returns null

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("不支持的文件格式")
        }
    }

    @Test
    fun `should handle empty comic file error`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { mockComicParser.getPageCount() } returns 0

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("文件中没有找到有效的图片")
        }
    }

    @Test
    fun `should handle loading exception`() = runTest {
        val exception = RuntimeException("Loading failed")
        coEvery { mockGetMangaByIdUseCase(testMangaId) } throws exception

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
            assertThat(state.error).isEqualTo("加载漫画失败: Loading failed")
        }
    }

    // === Page Navigation Tests ===

    @Test
    fun `nextPage should advance to next page when not at end`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(51)
            assertThat(state.readingProgress).isEqualTo(0.51f)
        }
    }

    @Test
    fun `nextPage should not advance beyond last page`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        // Start at last page
        val lastPageManga = testManga.copy(currentPage = 99)
        coEvery { mockGetMangaByIdUseCase(testMangaId) } returns lastPageManga

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.nextPage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(99) // Should remain at last page
        }
    }

    @Test
    fun `previousPage should go back to previous page when not at beginning`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.previousPage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(49)
            assertThat(state.readingProgress).isEqualTo(0.49f)
        }
    }

    @Test
    fun `previousPage should not go before first page`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        // Start at first page
        val firstPageManga = testManga.copy(currentPage = 0)
        coEvery { mockGetMangaByIdUseCase(testMangaId) } returns firstPageManga

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.previousPage()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(0) // Should remain at first page
        }
    }

    @Test
    fun `goToPage should navigate to valid page index`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.goToPage(75)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(75)
            assertThat(state.readingProgress).isEqualTo(0.75f)
        }
    }

    @Test
    fun `goToPage should clamp invalid page indices`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Test negative page
        viewModel.goToPage(-5)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(0) // Should clamp to 0
        }

        // Test page beyond count
        viewModel.goToPage(150)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(99) // Should clamp to last page
        }
    }

    @Test
    fun `goToPage should not trigger save if page doesn't change`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(mockUpdateReadingProgressUseCase) // Clear setup calls

        // Go to same page
        viewModel.goToPage(50) // Same as current page
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not save progress since page didn't change
        coVerify(exactly = 0) { mockUpdateReadingProgressUseCase(any()) }
    }

    // === Progress Saving Tests ===

    @Test
    fun `should save progress with debouncing after page change`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(mockUpdateReadingProgressUseCase) // Clear setup calls

        viewModel.goToPage(75)
        
        // Before debounce delay
        testDispatcher.scheduler.advanceTimeBy(200L)
        coVerify(exactly = 0) { mockUpdateReadingProgressUseCase(any()) }

        // After debounce delay
        testDispatcher.scheduler.advanceTimeBy(200L)
        
        coVerify { 
            mockUpdateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = testMangaId,
                    currentPage = 75,
                    pageCount = 100
                )
            )
        }
    }

    @Test
    fun `multiple rapid page changes should debounce to last page`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(mockUpdateReadingProgressUseCase)

        // Rapid page changes
        viewModel.goToPage(60)
        viewModel.goToPage(70)
        viewModel.goToPage(80)
        
        // Wait for debounce
        testDispatcher.scheduler.advanceTimeBy(400L)
        
        // Should only save the last page
        coVerify(exactly = 1) { 
            mockUpdateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = testMangaId,
                    currentPage = 80,
                    pageCount = 100
                )
            )
        }
    }

    @Test
    fun `should handle save progress error gracefully`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        
        val exception = RuntimeException("Save failed")
        coEvery { mockUpdateReadingProgressUseCase(any()) } throws exception

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash
        viewModel.goToPage(75)
        testDispatcher.scheduler.advanceTimeBy(400L)

        // Verify attempt was made
        coVerify { mockUpdateReadingProgressUseCase(any()) }
    }

    // === Menu and Settings Tests ===

    @Test
    fun `toggleMenu should toggle menu visibility`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleMenu()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.isMenuVisible).isTrue()
        }

        viewModel.toggleMenu()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.isMenuVisible).isFalse()
        }
    }

    @Test
    fun `menu should auto-hide after delay when made visible`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleMenu()

        // Initially visible
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.isMenuVisible).isTrue()
        }

        // After auto-hide delay
        testDispatcher.scheduler.advanceTimeBy(3100L) // 3 seconds + buffer

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.isMenuVisible).isFalse()
        }
    }

    @Test
    fun `setReadingMode should update reading mode setting`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setReadingMode(ReadingMode.FIT_WIDTH)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.readingMode).isEqualTo(ReadingMode.FIT_WIDTH)
        }
    }

    @Test
    fun `setReadingDirection should update reading direction setting`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setReadingDirection(ReadingDirection.RIGHT_TO_LEFT)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.readingDirection).isEqualTo(ReadingDirection.RIGHT_TO_LEFT)
        }
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Start with an error state
        coEvery { mockGetMangaByIdUseCase(testMangaId) } returns null

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify error exists
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.error).isNotNull()
        }

        // Clear error
        viewModel.clearError()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.error).isNull()
        }
    }

    // === Bitmap Loading Tests ===

    @Test
    fun `getPageBitmap should return bitmap from cache if available`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // First call should load and cache
        val bitmap1 = viewModel.getPageBitmap(25)
        assertThat(bitmap1).isEqualTo(mockBitmap)

        // Second call should return cached bitmap
        val bitmap2 = viewModel.getPageBitmap(25)
        assertThat(bitmap2).isEqualTo(mockBitmap)

        // Should only call parser once (first time)
        verify(exactly = 1) { mockComicParser.getPageStream(25) }
    }

    @Test
    fun `getPageBitmap should handle page loading error gracefully`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { mockComicParser.getPageStream(any()) } throws RuntimeException("Failed to read page")

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val bitmap = viewModel.getPageBitmap(25)
        assertThat(bitmap).isNull()
    }

    @Test
    fun `getPageBitmap should handle decode failure gracefully`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { android.graphics.BitmapFactory.decodeStream(any()) } returns null

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val bitmap = viewModel.getPageBitmap(25)
        assertThat(bitmap).isNull()
    }

    // === Cleanup Tests ===

    @Test
    fun `onCleared should save progress and cleanup resources`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(mockUpdateReadingProgressUseCase) // Clear setup calls

        // Change page but don't wait for debounce
        viewModel.goToPage(75)

        // Simulate ViewModel being cleared
        viewModel.onCleared()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should save progress immediately on clear
        coVerify { 
            mockUpdateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = testMangaId,
                    currentPage = 75,
                    pageCount = 100
                )
            )
        }

        // Should close parser
        verify { mockComicParser.close() }
        
        // Should recycle cached bitmaps
        verify { mockBitmap.recycle() }
    }

    @Test
    fun `onCleared should handle cleanup errors gracefully`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true
        every { mockComicParser.close() } throws RuntimeException("Close failed")

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash during cleanup
        viewModel.onCleared()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { mockComicParser.close() }
    }

    // === Integration Tests ===

    @Test
    fun `complete reading session workflow should work correctly`() = runTest {
        mockkConstructor(File::class)
        every { anyConstructed<File>().exists() } returns true

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Initial state
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(50)
            assertThat(state.pageCount).isEqualTo(100)
        }

        // Navigate through pages
        viewModel.nextPage()
        viewModel.nextPage()
        viewModel.previousPage()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should end up at page 51
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.currentPage).isEqualTo(51)
        }

        // Toggle menu
        viewModel.toggleMenu()
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.isMenuVisible).isTrue()
        }

        // Change settings
        viewModel.setReadingMode(ReadingMode.FIT_HEIGHT)
        viewModel.setReadingDirection(ReadingDirection.RIGHT_TO_LEFT)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.settings.readingMode).isEqualTo(ReadingMode.FIT_HEIGHT)
            assertThat(state.settings.readingDirection).isEqualTo(ReadingDirection.RIGHT_TO_LEFT)
        }

        // Wait for progress save
        testDispatcher.scheduler.advanceTimeBy(400L)

        // Verify progress was saved
        coVerify { 
            mockUpdateReadingProgressUseCase(
                UpdateReadingProgressUseCase.Params(
                    mangaId = testMangaId,
                    currentPage = 51,
                    pageCount = 100
                )
            )
        }
    }
}
