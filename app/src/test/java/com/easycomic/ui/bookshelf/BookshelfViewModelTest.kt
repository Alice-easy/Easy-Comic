package com.easycomic.ui.bookshelf

import android.net.Uri
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.model.ImportComicResult
import com.easycomic.domain.model.BatchImportComicResult
import com.easycomic.domain.model.ImportProgress
import com.easycomic.domain.model.ImportStatus
import com.easycomic.domain.usecase.manga.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * BookshelfViewModel 测试类
 */
@RunWith(MockitoJUnitRunner::class)
class BookshelfViewModelTest {

    @Mock
    private lateinit var getAllMangaUseCase: GetAllMangaUseCase

    @Mock
    private lateinit var searchMangaUseCase: SearchMangaUseCase

    @Mock
    private lateinit var getFavoriteMangaUseCase: GetFavoriteMangaUseCase

    @Mock
    private lateinit var getRecentMangaUseCase: GetRecentMangaUseCase

    @Mock
    private lateinit var deleteMangaUseCase: DeleteMangaUseCase

    @Mock
    private lateinit var deleteAllMangaUseCase: DeleteAllMangaUseCase

    @Mock
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase

    @Mock
    private lateinit var importComicUseCase: ImportComicUseCase

    @Mock
    private lateinit var batchImportComicsUseCase: BatchImportComicsUseCase

    @Mock
    private lateinit var monitorImportProgressUseCase: MonitorImportProgressUseCase

    private lateinit var viewModel: BookshelfViewModel

    @Before
    fun setup() {
        viewModel = BookshelfViewModel(
            getAllMangaUseCase,
            searchMangaUseCase,
            getFavoriteMangaUseCase,
            getRecentMangaUseCase,
            deleteMangaUseCase,
            deleteAllMangaUseCase,
            toggleFavoriteUseCase,
            importComicUseCase,
            batchImportComicsUseCase,
            monitorImportProgressUseCase
        )
    }

    @Test
    fun `when initialized, should load all manga and setup monitoring`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })
        whenever(monitorImportProgressUseCase()).thenReturn(flow { emit(ImportProgress()) })

        // When
        viewModel.uiState.test {
            // Then
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

    @Test
    fun `when importing comic, should update import state correctly`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/comic.cbz")
        val mockResult = ImportComicResult(
            success = true,
            mangaId = 1L,
            manga = createMockManga(id = 1L)
        )
        
        whenever(importComicUseCase(uri)).thenReturn(flow { emit(mockResult) })
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(listOf(createMockManga(id = 1L))) })

        // When
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

    @Test
    fun `when batch importing comics, should update import state correctly`() = runTest {
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
        
        whenever(batchImportComicsUseCase(uris)).thenReturn(flow { emit(mockResult) })
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(emptyList()) })

        // When
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

    @Test
    fun `when import fails, should handle error gracefully`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/error.cbz")
        val exception = RuntimeException("导入失败")
        
        whenever(importComicUseCase(uri)).thenReturn(flow { throw exception })

        // When
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

    @Test
    fun `when canceling import, should reset import state`() = runTest {
        // Given
        val uri = Uri.parse("content://com.example.provider/comic.cbz")
        whenever(importComicUseCase(uri)).thenReturn(flow { emit(ImportComicResult(success = false)) })

        // When
        viewModel.importComic(uri)
        viewModel.cancelImport()

        // Then
        viewModel.importState.test {
            val canceledState = awaitItem()
            assertFalse(canceledState.isImporting)
            assertNull(canceledState.currentImportUri)
            assertNull(canceledState.currentImportResult)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when searching manga, should update search results`() = runTest {
        // Given
        val query = "测试"
        val searchResults = listOf(
            createMockManga(id = 1L, title = "测试漫画"),
            createMockManga(id = 2L, title = "另一个测试")
        )
        
        whenever(searchMangaUseCase(query)).thenReturn(flow { emit(searchResults) })

        // When
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

    @Test
    fun `when setting sort option, should update sort and apply to filtered list`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "B漫画"),
            createMockManga(id = 2L, title = "A漫画")
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })

        // When
        viewModel.setSortOption(SortOption.TITLE_ASC)

        // Then
        viewModel.uiState.test {
            val sortedState = awaitItem()
            assertEquals(2, sortedState.filteredMangaList.size)
            assertEquals("A漫画", sortedState.filteredMangaList[0].title)
            assertEquals("B漫画", sortedState.filteredMangaList[1].title)
            
            cancelAndIgnoreRemainingEvents()
        }
        
        assertEquals(SortOption.TITLE_ASC, viewModel.sortOption.value)
    }

    @Test
    fun `when setting filter option, should update filter and apply to filtered list`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1", isFavorite = true),
            createMockManga(id = 2L, title = "漫画2", isFavorite = false)
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })

        // When
        viewModel.setFilterOption(FilterOption.FAVORITES)

        // Then
        viewModel.uiState.test {
            val filteredState = awaitItem()
            assertEquals(1, filteredState.filteredMangaList.size)
            assertTrue(filteredState.filteredMangaList[0].isFavorite)
            assertEquals("漫画1", filteredState.filteredMangaList[0].title)
            
            cancelAndIgnoreRemainingEvents()
        }
        
        assertEquals(FilterOption.FAVORITES, viewModel.filterOption.value)
    }

    @Test
    fun `when toggling selection mode, should update selection state`() = runTest {
        // Given
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(emptyList()) })

        // When
        viewModel.toggleSelectionMode()

        // Then
        assertTrue(viewModel.selectionMode.value)

        // When - toggle again
        viewModel.toggleSelectionMode()

        // Then
        assertFalse(viewModel.selectionMode.value)
        assertTrue(viewModel.selectedMangaIds.value.isEmpty())
    }

    @Test
    fun `when toggling manga selection, should update selected manga ids`() = runTest {
        // Given
        val mangaId = 1L
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(emptyList()) })

        // When
        viewModel.toggleMangaSelection(mangaId)

        // Then
        assertTrue(viewModel.selectedMangaIds.value.contains(mangaId))

        // When - toggle again
        viewModel.toggleMangaSelection(mangaId)

        // Then
        assertFalse(viewModel.selectedMangaIds.value.contains(mangaId))
    }

    @Test
    fun `when selecting all manga, should select all in filtered list`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })

        // When
        viewModel.selectAll()

        // Then
        assertEquals(2, viewModel.selectedMangaIds.value.size)
        assertTrue(viewModel.selectedMangaIds.value.contains(1L))
        assertTrue(viewModel.selectedMangaIds.value.contains(2L))
    }

    @Test
    fun `when deselecting all manga, should clear selection`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })
        viewModel.selectAll()

        // When
        viewModel.deselectAll()

        // Then
        assertTrue(viewModel.selectedMangaIds.value.isEmpty())
    }

    @Test
    fun `when deleting selected manga, should call delete use case and update state`() = runTest {
        // Given
        val mockMangaList = listOf(
            createMockManga(id = 1L, title = "漫画1"),
            createMockManga(id = 2L, title = "漫画2")
        )
        
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })
        whenever(deleteAllMangaUseCase(any())).thenReturn(Unit)

        // When
        viewModel.selectAll()
        viewModel.deleteSelectedManga()

        // Then
        verify(deleteAllMangaUseCase).invoke(mockMangaList)
        assertTrue(viewModel.selectedMangaIds.value.isEmpty())
        assertFalse(viewModel.selectionMode.value)
    }

    @Test
    fun `when toggling favorite, should call toggle favorite use case`() = runTest {
        // Given
        val mangaId = 1L
        whenever(toggleFavoriteUseCase(mangaId)).thenReturn(Unit)

        // When
        viewModel.toggleFavorite(mangaId)

        // Then
        verify(toggleFavoriteUseCase).invoke(mangaId)
    }

    @Test
    fun `when monitoring import progress, should update progress state`() = runTest {
        // Given
        val progress = ImportProgress(
            status = ImportStatus.PARSING,
            progress = 50,
            message = "正在解析..."
        )
        
        whenever(monitorImportProgressUseCase()).thenReturn(flow { emit(progress) })
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(emptyList()) })

        // When - ViewModel initializes with monitoring

        // Then
        viewModel.importProgress.test {
            val progressState = awaitItem()
            assertEquals(ImportStatus.PARSING, progressState.status)
            assertEquals(50, progressState.progress)
            assertEquals("正在解析...", progressState.message)
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when import completes, should reset import state and refresh manga list`() = runTest {
        // Given
        val progress = ImportProgress(
            status = ImportStatus.COMPLETED,
            progress = 100,
            message = "导入完成"
        )
        
        val mockMangaList = listOf(createMockManga(id = 1L, title = "新漫画"))
        
        whenever(monitorImportProgressUseCase()).thenReturn(flow { emit(progress) })
        whenever(getAllMangaUseCase()).thenReturn(flow { emit(mockMangaList) })

        // When - ViewModel initializes with monitoring

        // Then
        viewModel.importState.test {
            val resetState = awaitItem()
            assertFalse(resetState.isImporting)
            assertNull(resetState.currentImportResult)
            assertNull(resetState.batchImportResult)
            
            cancelAndIgnoreRemainingEvents()
        }
        
        viewModel.uiState.test {
            val refreshedState = awaitItem()
            assertEquals(1, refreshedState.mangaList.size)
            assertEquals("新漫画", refreshedState.mangaList[0].title)
            
            cancelAndIgnoreRemainingEvents()
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