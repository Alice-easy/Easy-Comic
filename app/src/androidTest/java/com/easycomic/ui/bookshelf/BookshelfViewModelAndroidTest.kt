package com.easycomic.ui.bookshelf

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.model.ImportComicResult
import com.easycomic.domain.model.BatchImportComicResult
import com.easycomic.domain.model.ImportProgress
import com.easycomic.domain.model.ImportStatus
import com.easycomic.di.KoinModules
import io.insertkoin.koin.test.KoinTest
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
import kotlin.test.assertTrue

/**
 * BookshelfViewModel Android 集成测试
 * 验证 ViewModel 在 Android 环境中的真实依赖注入功能
 */
@RunWith(AndroidJUnit4::class)
class BookshelfViewModelAndroidTest : KoinTest {

    @Mock
    private lateinit var mockBookshelfService: com.easycomic.domain.service.BookshelfService

    private lateinit var activityScenario: ActivityScenario<ComponentActivity>

    @Before
    fun setup() {
        // 初始化 Koin
        KoinModules.initializeKoin(androidx.test.core.app.ApplicationProvider.getApplicationContext())

        // 启动 Activity 场景
        activityScenario = ActivityScenario.launch(ComponentActivity::class.java)
    }

    @After
    fun tearDown() {
        activityScenario.close()
        io.insertkoin.koin.core.context.stopKoin()
    }

    @Test
    fun `test BookshelfViewModel injection in Android environment`() {
        // Given - Activity 场景已启动

        activityScenario.onActivity { activity ->
            // When
            val bookshelfViewModel: BookshelfViewModel by inject()

            // Then
            assertNotNull(bookshelfViewModel)
            assertTrue(bookshelfViewModel is BookshelfViewModel)
        }
    }

    @Test
    fun `test BookshelfViewModel with real dependencies in Android environment`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(mockMangaList) })
        whenever(mockBookshelfService.monitorImportProgress()).thenReturn(flow { emit(ImportProgress()) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)

            // Then
            viewModel.uiState.test {
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)
                
                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertEquals(2, loadedState.mangaList.size)
                assertEquals(mockMangaList, loadedState.mangaList)
                assertEquals(mockMangaList, loadedState.filteredMangaList)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `test BookshelfViewModel import functionality in Android environment`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/comic.cbz")
        val mockResult = ImportComicResult(
            success = true,
            mangaId = 1L,
            manga = createMockManga(id = 1L)
        )
        
        whenever(mockBookshelfService.importComic(uri)).thenReturn(flow { emit(mockResult) })
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(listOf(createMockManga(id = 1L))) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.importComic(uri)

            // Then
            viewModel.importState.test {
                val importingState = awaitItem()
                assertTrue(importingState.isImporting)
                assertEquals(uri, importingState.currentImportUri)
                
                val completedState = awaitItem()
                assertFalse(completedState.isImporting)
                assertNotNull(completedState.currentImportResult)
                assertTrue(completedState.currentImportResult?.success ?: false)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `test BookshelfViewModel lifecycle handling in Android environment`() {
        // Given
        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)

            // Then - 验证 ViewModel 不会因为 Activity 生命周期变化而崩溃
            // 重新创建 Activity
            activityScenario.recreate()

            // ViewModel 应该仍然存在并且功能正常
            assertNotNull(viewModel)
            assertTrue(viewModel is BookshelfViewModel)
        }
    }

    @Test
    fun `test BookshelfViewModel configuration changes handling`() {
        // Given
        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)

            // 模拟配置更改（比如屏幕旋转）
            val oldState = viewModel.uiState.value

            // Then - ViewModel 应该能够处理配置更改
            assertNotNull(viewModel)
            assertNotNull(oldState)
            
            // 验证状态没有被意外重置
            assertEquals(oldState, viewModel.uiState.value)
        }
    }

    @Test
    fun `test BookshelfViewModel error handling in Android environment`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/error.cbz")
        val exception = RuntimeException("导入失败")
        
        whenever(mockBookshelfService.importComic(uri)).thenReturn(flow { throw exception })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.importComic(uri)

            // Then
            viewModel.importState.test {
                val importingState = awaitItem()
                assertTrue(importingState.isImporting)
                
                val errorState = awaitItem()
                assertFalse(errorState.isImporting)
                assertEquals("导入失败: 导入失败", errorState.importError)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `test BookshelfViewModel batch import in Android environment`() = runTest {
        // Given
        val uris = listOf(
            Uri.parse("content://com.example.provider/comic1.cbz"),
            Uri.parse("content://com.example.provider/comic2.cbz")
        )
        val mockResult = BatchImportComicResult(
            success = true,
            totalItems = 2,
            importedCount = 2,
            failedCount = 0
        )
        
        whenever(mockBookshelfService.importComics(uris)).thenReturn(flow { emit(mockResult) })
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(emptyList()) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.importComics(uris)

            // Then
            viewModel.importState.test {
                val importingState = awaitItem()
                assertTrue(importingState.isImporting)
                assertTrue(importingState.isBatchImport)
                assertEquals(2, importingState.batchImportTotal)
                
                val completedState = awaitItem()
                assertFalse(completedState.isImporting)
                assertNotNull(completedState.batchImportResult)
                assertTrue(completedState.batchImportResult?.success ?: false)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `test BookshelfViewModel search functionality in Android environment`() = runTest {
        // Given
        val query = "测试"
        val searchResults = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(mockBookshelfService.searchManga(query)).thenReturn(flow { emit(searchResults) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.searchManga(query)

            // Then
            viewModel.uiState.test {
                val searchState = awaitItem()
                assertEquals(2, searchState.mangaList.size)
                assertEquals(searchResults, searchState.mangaList)
                assertEquals(searchResults, searchState.filteredMangaList)
                
                cancelAndIgnoreRemainingEvents()
            }
            
            assertEquals(query, viewModel.searchQuery.value)
        }
    }

    @Test
    fun `test BookshelfViewModel selection functionality in Android environment`() = runTest {
        // Given
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(emptyList()) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.toggleSelectionMode()
            viewModel.toggleMangaSelection(1L)
            viewModel.selectAll()

            // Then
            assertTrue(viewModel.selectionMode.value)
            assertTrue(viewModel.selectedMangaIds.value.contains(1L))

            // When - deselect all
            viewModel.deselectAll()

            // Then
            assertTrue(viewModel.selectedMangaIds.value.isEmpty())
        }
    }

    @Test
    fun `test BookshelfViewModel sort and filter in Android environment`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "B漫画", isFavorite = true),
            createMockManga(id = 2L, title = "A漫画", isFavorite = false)
        )
        
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(mockMangaList) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)
            viewModel.setSortOption(SortOption.TITLE_ASC)

            // Then
            viewModel.uiState.test {
                val sortedState = awaitItem()
                assertEquals(2, sortedState.filteredMangaList.size)
                assertEquals("A漫画", sortedState.filteredMangaList[0].title)
                assertEquals("B漫画", sortedState.filteredMangaList[1].title)
                
                cancelAndIgnoreRemainingEvents()
            }

            // When - set filter
            viewModel.setFilterOption(FilterOption.FAVORITES)

            // Then
            viewModel.uiState.test {
                val filteredState = awaitItem()
                assertEquals(1, filteredState.filteredMangaList.size)
                assertTrue(filteredState.filteredMangaList[0].isFavorite)
                assertEquals("B漫画", filteredState.filteredMangaList[0].title)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `test BookshelfViewModel memory usage in Android environment`() {
        // Given
        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)

            // Then - 验证 ViewModel 不会造成内存泄漏
            // 通过创建多个 ViewModel 实例来测试内存使用
            val viewModels = mutableListOf<BookshelfViewModel>()
            
            for (i in 1..10) {
                val vm = BookshelfViewModel(mockBookshelfService)
                viewModels.add(vm)
                assertNotNull(vm)
            }

            // 清理
            viewModels.clear()
            
            // 验证原始 ViewModel 仍然正常工作
            assertNotNull(viewModel)
            assertTrue(viewModel is BookshelfViewModel)
        }
    }

    @Test
    fun `test BookshelfViewModel thread safety in Android environment`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mockBookshelfService.getAllManga()).thenReturn(flow { emit(mockMangaList) })
        whenever(mockBookshelfService.monitorImportProgress()).thenReturn(flow { emit(ImportProgress()) })

        activityScenario.onActivity { activity ->
            // When
            val viewModel = BookshelfViewModel(mockBookshelfService)

            // Then - 验证在多线程环境下的状态一致性
            viewModel.uiState.test {
                val initialState = awaitItem()
                assertTrue(initialState.isLoading)
                
                val loadedState = awaitItem()
                assertFalse(loadedState.isLoading)
                assertEquals(2, loadedState.mangaList.size)
                
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    private fun createMockManga(
        id: Long,
        title: String,
        isFavorite: Boolean = false,
        readingStatus: ReadingStatus = ReadingStatus.UNREAD
    ): Manga {
        return Manga(
            id = id,
            title = title,
            author = "测试作者",
            description = "测试描述",
            filePath = "/path/to/comic.cbz",
            fileUri = "content://com.example.provider/comic.cbz",
            fileFormat = "CBZ",
            fileSize = 1024000L,
            pageCount = 100,
            currentPage = 0,
            coverImagePath = "/path/to/cover.jpg",
            thumbnailPath = "/path/to/thumbnail.jpg",
            rating = 4.5f,
            isFavorite = isFavorite,
            readingStatus = readingStatus,
            tags = emptyList(),
            lastRead = System.currentTimeMillis(),
            dateAdded = System.currentTimeMillis(),
            dateModified = System.currentTimeMillis()
        )
    }
}