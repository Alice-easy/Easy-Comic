package com.easycomic.domain.service

import android.net.Uri
import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.model.ImportComicResult
import com.easycomic.domain.model.BatchImportComicResult
import com.easycomic.domain.model.ImportProgress
import com.easycomic.domain.model.ImportStatus
import com.easycomic.domain.usecase.manga.*
import com.easycomic.di.KoinModules
import io.insertkoin.koin.test.KoinTest
import io.insertkoin.koin.test.get
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * BookshelfService Koin 注入测试
 * 验证 BookshelfService 通过 Koin 正确注入依赖
 */
@RunWith(MockitoJUnitRunner::class)
class BookshelfServiceKoinTest : KoinTest {

    @Mock
    private lateinit var mockGetAllMangaUseCase: GetAllMangaUseCase

    @Mock
    private lateinit var mockSearchMangaUseCase: SearchMangaUseCase

    @Mock
    private lateinit var mockGetFavoriteMangaUseCase: GetFavoriteMangaUseCase

    @Mock
    private lateinit var mockGetRecentMangaUseCase: GetRecentMangaUseCase

    @Mock
    private lateinit var mockDeleteMangaUseCase: DeleteMangaUseCase

    @Mock
    private lateinit var mockDeleteAllMangaUseCase: DeleteAllMangaUseCase

    @Mock
    private lateinit var mockToggleFavoriteUseCase: ToggleFavoriteUseCase

    @Mock
    private lateinit var mockImportComicUseCase: ImportComicUseCase

    @Mock
    private lateinit var mockBatchImportComicsUseCase: BatchImportComicsUseCase

    @Mock
    private lateinit var mockMonitorImportProgressUseCase: MonitorImportProgressUseCase

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
    fun `test BookshelfService can be injected through Koin`() {
        // Given - Koin 已初始化

        // When
        val bookshelfService: BookshelfService = get()

        // Then
        assertNotNull(bookshelfService)
        assertTrue(bookshelfService is BookshelfService)
    }

    @Test
    fun `test BookshelfService getAllManga functionality with Koin`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(mockGetAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.getAllManga()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(mockMangaList, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService searchManga functionality with Koin`() = runTest {
        // Given
        val query = "测试"
        val searchResults = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(mockSearchMangaUseCase(query)).thenReturn(flow { emit(searchResults) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.searchManga(query)

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(searchResults, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService getFavoriteManga functionality with Koin`() = runTest {
        // Given
        val favoriteMangaList = listOf(
            createMockManga(id = 1L, title = "收藏漫画1", isFavorite = true),
            createMockManga(id = 2L, title = "收藏漫画2", isFavorite = true)
        )
        
        whenever(mockGetFavoriteMangaUseCase()).thenReturn(flow { emit(favoriteMangaList) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.getFavoriteManga()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(favoriteMangaList, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService getRecentManga functionality with Koin`() = runTest {
        // Given
        val recentMangaList = listOf(
            createMockManga(id = 1L, title = "最近漫画1"),
            createMockManga(id = 2L, title = "最近漫画2")
        )
        
        whenever(mockGetRecentMangaUseCase()).thenReturn(flow { emit(recentMangaList) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.getRecentManga()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertEquals(recentMangaList, emittedList)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService deleteManga functionality with Koin`() = runTest {
        // Given
        val mangaToDelete = createMockManga(id = 1L, title = "删除漫画")
        whenever(mockDeleteMangaUseCase(mangaToDelete)).thenReturn(Unit)

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        bookshelfService.deleteManga(mangaToDelete)

        // Then
        verify(mockDeleteMangaUseCase).invoke(mangaToDelete)
    }

    @Test
    fun `test BookshelfService deleteAllManga functionality with Koin`() = runTest {
        // Given
        val mangaListToDelete = listOf(
            createMockManga(id = 1L, title = "删除漫画1"),
            createMockManga(id = 2L, title = "删除漫画2")
        )
        whenever(mockDeleteMangaUseCase(mangaListToDelete[0])).thenReturn(Unit)
        whenever(mockDeleteMangaUseCase(mangaListToDelete[1])).thenReturn(Unit)

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        bookshelfService.deleteAllManga(mangaListToDelete)

        // Then
        verify(mockDeleteMangaUseCase).invoke(mangaListToDelete[0])
        verify(mockDeleteMangaUseCase).invoke(mangaListToDelete[1])
    }

    @Test
    fun `test BookshelfService deleteAllManga without parameters with Koin`() = runTest {
        // Given
        whenever(mockDeleteAllMangaUseCase()).thenReturn(Unit)

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        bookshelfService.deleteAllManga()

        // Then
        verify(mockDeleteAllMangaUseCase).invoke()
    }

    @Test
    fun `test BookshelfService toggleFavorite functionality with Koin`() = runTest {
        // Given
        val mangaId = 1L
        whenever(mockToggleFavoriteUseCase(mangaId)).thenReturn(Unit)

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        bookshelfService.toggleFavorite(mangaId)

        // Then
        verify(mockToggleFavoriteUseCase).invoke(mangaId)
    }

    @Test
    fun `test BookshelfService importComic functionality with Koin`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/comic.cbz")
        val mockResult = ImportComicResult(
            success = true,
            mangaId = 1L,
            manga = createMockManga(id = 1L)
        )
        
        whenever(mockImportComicUseCase(uri)).thenReturn(flow { emit(mockResult) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.importComic(uri)

        // Then
        result.test {
            val emittedResult = awaitItem()
            assertTrue(emittedResult.success)
            assertEquals(1L, emittedResult.mangaId)
            assertNotNull(emittedResult.manga)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService importComics functionality with Koin`() = runTest {
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
        
        whenever(mockBatchImportComicsUseCase(uris)).thenReturn(flow { emit(mockResult) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.importComics(uris)

        // Then
        result.test {
            val emittedResult = awaitItem()
            assertTrue(emittedResult.success)
            assertEquals(2, emittedResult.totalItems)
            assertEquals(2, emittedResult.importedCount)
            assertEquals(0, emittedResult.failedCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService monitorImportProgress functionality with Koin`() = runTest {
        // Given
        val progress = ImportProgress(
            status = ImportStatus.PARSING,
            progress = 50,
            message = "正在解析..."
        )
        
        whenever(mockMonitorImportProgressUseCase()).thenReturn(flow { emit(progress) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.monitorImportProgress()

        // Then
        result.test {
            val emittedProgress = awaitItem()
            assertEquals(ImportStatus.PARSING, emittedProgress.status)
            assertEquals(50, emittedProgress.progress)
            assertEquals("正在解析...", emittedProgress.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService with empty results with Koin`() = runTest {
        // Given
        whenever(mockGetAllMangaUseCase()).thenReturn(flow { emit(emptyList()) })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.getAllManga()

        // Then
        result.test {
            val emittedList = awaitItem()
            assertTrue(emittedList.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `test BookshelfService with error results with Koin`() = runTest {
        // Given
        val exception = RuntimeException("获取数据失败")
        whenever(mockGetAllMangaUseCase()).thenReturn(flow { throw exception })

        // When
        val bookshelfService = createBookshelfServiceWithMocks()
        val result = bookshelfService.getAllManga()

        // Then
        result.test {
            // 应该抛出异常
            val error = awaitError()
            assertEquals("获取数据失败", error.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /**
     * 创建带有 mock 依赖的 BookshelfService
     */
    private fun createBookshelfServiceWithMocks(): BookshelfService {
        return BookshelfService(
            getAllMangaUseCase = mockGetAllMangaUseCase,
            searchMangaUseCase = mockSearchMangaUseCase,
            getFavoriteMangaUseCase = mockGetFavoriteMangaUseCase,
            getRecentMangaUseCase = mockGetRecentMangaUseCase,
            deleteMangaUseCase = mockDeleteMangaUseCase,
            deleteAllMangaUseCase = mockDeleteAllMangaUseCase,
            toggleFavoriteUseCase = mockToggleFavoriteUseCase,
            importComicUseCase = mockImportComicUseCase,
            batchImportComicsUseCase = mockBatchImportComicsUseCase,
            monitorImportProgressUseCase = mockMonitorImportProgressUseCase
        )
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