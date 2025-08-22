package com.easycomic.ui_bookshelf

import android.net.Uri
import app.cash.turbine.test
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.data.repository.ImportProgress
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import com.easycomic.domain.usecase.manga.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    // Mock dependencies
    private val mockGetAllMangaUseCase = mockk<GetAllMangaUseCase>()
    private val mockImportComicsUseCase = mockk<ImportComicsUseCase>()
    private val mockComicImportRepository = mockk<ComicImportRepositoryImpl>()
    private val mockDeleteComicsUseCase = mockk<DeleteComicsUseCase>()
    private val mockUpdateMangaFavoriteStatusUseCase = mockk<UpdateMangaFavoriteStatusUseCase>()
    private val mockMarkMangasAsReadUseCase = mockk<MarkMangasAsReadUseCase>()

    private lateinit var viewModel: BookshelfViewModel
    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val testManga1 = Manga(
        id = 1L,
        title = "Test Manga 1",
        author = "Author 1",
        filePath = "/path/to/manga1.zip",
        totalPages = 100,
        currentPage = 50,
        isFavorite = false,
        readingStatus = ReadingStatus.READING,
        dateAdded = 1000L,
        lastRead = 2000L
    )

    private val testManga2 = Manga(
        id = 2L,
        title = "Another Comic",
        author = "Author 2",
        filePath = "/path/to/manga2.zip",
        totalPages = 200,
        currentPage = 0,
        isFavorite = true,
        readingStatus = ReadingStatus.UNREAD,
        dateAdded = 1500L,
        lastRead = null
    )

    private val testMangaList = listOf(testManga1, testManga2)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Default behavior for getAllMangaUseCase
        coEvery { mockGetAllMangaUseCase() } returns flowOf(testMangaList)
        
        // Default behavior for other use cases
        coEvery { mockImportComicsUseCase(any()) } returns Unit
        coEvery { mockDeleteComicsUseCase.deleteMultiple(any()) } returns Unit
        coEvery { mockUpdateMangaFavoriteStatusUseCase.updateMultiple(any(), any()) } returns Unit
        coEvery { mockMarkMangasAsReadUseCase.markMultipleAsRead(any()) } returns Unit
        
        // Default behavior for import repository
        every { mockComicImportRepository.importFromDocumentTree(any()) } returns flowOf(
            ImportProgress.Completed(1, 0)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    private fun createViewModel(): BookshelfViewModel {
        return BookshelfViewModel(
            getAllMangaUseCase = mockGetAllMangaUseCase,
            importComicsUseCase = mockImportComicsUseCase,
            comicImportRepository = mockComicImportRepository,
            deleteComicsUseCase = mockDeleteComicsUseCase,
            updateMangaFavoriteStatusUseCase = mockUpdateMangaFavoriteStatusUseCase,
            markMangasAsReadUseCase = mockMarkMangasAsReadUseCase
        )
    }

    // === Initial State Tests ===

    @Test
    fun `initial state should load comics on creation`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).isEqualTo(testMangaList)
        }

        viewModel.isLoading.test {
            val isLoading = awaitItem()
            assertThat(isLoading).isFalse()
        }

        coVerify { mockGetAllMangaUseCase() }
    }

    @Test
    fun `should handle loading error gracefully`() = runTest {
        val exception = RuntimeException("Failed to load comics")
        coEvery { mockGetAllMangaUseCase() } returns flow { throw exception }

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isLoading.test {
            val isLoading = awaitItem()
            assertThat(isLoading).isFalse()
        }

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).isEmpty()
        }
    }

    // === Search Functionality Tests ===

    @Test
    fun `searchComics should filter by title case insensitive`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchComics("test")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).hasSize(1)
            assertThat(comics.first().title).isEqualTo("Test Manga 1")
        }
    }

    @Test
    fun `searchComics should filter by author case insensitive`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchComics("author 2")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).hasSize(1)
            assertThat(comics.first().author).isEqualTo("Author 2")
        }
    }

    @Test
    fun `searchComics with empty query should show all comics`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchComics("")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).hasSize(2)
        }
    }

    @Test
    fun `searchComics with no matches should return empty list`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchComics("nonexistent")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).isEmpty()
        }
    }

    // === Sorting Tests ===

    @Test
    fun `setSortOrder TITLE_ASC should sort by title ascending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_ASC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics.first().title).isEqualTo("Another Comic")
            assertThat(comics.last().title).isEqualTo("Test Manga 1")
        }
    }

    @Test
    fun `setSortOrder TITLE_DESC should sort by title descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_DESC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics.first().title).isEqualTo("Test Manga 1")
            assertThat(comics.last().title).isEqualTo("Another Comic")
        }
    }

    @Test
    fun `setSortOrder DATE_ADDED_ASC should sort by date added ascending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSortOrder(BookshelfViewModel.SortOrder.DATE_ADDED_ASC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics.first().dateAdded).isEqualTo(1000L)
            assertThat(comics.last().dateAdded).isEqualTo(1500L)
        }
    }

    @Test
    fun `setSortOrder PROGRESS_DESC should sort by reading progress descending`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSortOrder(BookshelfViewModel.SortOrder.PROGRESS_DESC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            // testManga1 has progress 50/100 = 0.5
            // testManga2 has progress 0/200 = 0.0
            assertThat(comics.first().id).isEqualTo(1L) // Higher progress first
            assertThat(comics.last().id).isEqualTo(2L)
        }
    }

    // === Import Functionality Tests ===

    @Test
    fun `importComic should call usecase and reload comics`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val testPath = "/path/to/new/comic.zip"
        viewModel.importComic(testPath)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockImportComicsUseCase(File(testPath)) }
        coVerify(atLeast = 2) { mockGetAllMangaUseCase() } // Initial load + reload after import
    }

    @Test
    fun `importFromDocumentTree should update import progress states`() = runTest {
        val progressFlow = flow<ImportProgress> {
            emit(ImportProgress.Scanning)
            emit(ImportProgress.Processing("comic1.zip", 1, 2))
            emit(ImportProgress.Completed(2, 0))
        }
        every { mockComicImportRepository.importFromDocumentTree(any()) } returns progressFlow

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<Uri>()
        viewModel.importFromDocumentTree(mockUri)

        // Test import states
        viewModel.isImporting.test {
            assertThat(awaitItem()).isTrue() // Should be importing
        }

        viewModel.importProgress.test {
            val scanning = awaitItem()
            assertThat(scanning).isEqualTo(ImportProgress.Scanning)

            val processing = awaitItem()
            assertThat(processing).isInstanceOf(ImportProgress.Processing::class.java)

            val completed = awaitItem()
            assertThat(completed).isInstanceOf(ImportProgress.Completed::class.java)
        }

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isImporting.test {
            assertThat(awaitItem()).isFalse() // Should finish importing
        }

        verify { mockComicImportRepository.importFromDocumentTree(mockUri) }
        coVerify(atLeast = 2) { mockGetAllMangaUseCase() } // Initial + reload after import
    }

    @Test
    fun `importFromDocumentTree should handle import error`() = runTest {
        val errorFlow = flow<ImportProgress> {
            emit(ImportProgress.Scanning)
            emit(ImportProgress.Error("Import failed"))
        }
        every { mockComicImportRepository.importFromDocumentTree(any()) } returns errorFlow

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockUri = mockk<Uri>()
        viewModel.importFromDocumentTree(mockUri)

        viewModel.importProgress.test {
            val scanning = awaitItem()
            assertThat(scanning).isEqualTo(ImportProgress.Scanning)

            val error = awaitItem()
            assertThat(error).isInstanceOf(ImportProgress.Error::class.java)
            assertThat((error as ImportProgress.Error).message).isEqualTo("Import failed")
        }

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.isImporting.test {
            assertThat(awaitItem()).isFalse() // Should stop importing on error
        }
    }

    @Test
    fun `clearImportProgress should reset import progress state`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearImportProgress()

        viewModel.importProgress.test {
            assertThat(awaitItem()).isNull()
        }
    }

    // === Selection Mode Tests ===

    @Test
    fun `enterSelectionMode should enable selection mode with first manga`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isTrue()
        }

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).containsExactly(1L)
        }
    }

    @Test
    fun `clearSelection should disable selection mode and clear selections`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)
        viewModel.clearSelection()

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse()
        }

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).isEmpty()
        }
    }

    @Test
    fun `toggleMangaSelection should add and remove manga from selection`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)

        // Add second manga to selection
        viewModel.toggleMangaSelection(2L)

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).containsExactly(1L, 2L)
        }

        // Remove first manga from selection
        viewModel.toggleMangaSelection(1L)

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).containsExactly(2L)
        }

        // Remove last manga should exit selection mode
        viewModel.toggleMangaSelection(2L)

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse()
        }

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).isEmpty()
        }
    }

    @Test
    fun `selectAllVisibleMangas should select all provided mangas`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.selectAllVisibleMangas(testMangaList)

        viewModel.selectedMangas.test {
            val selected = awaitItem()
            assertThat(selected).containsExactly(1L, 2L)
        }
    }

    // === Batch Operations Tests ===

    @Test
    fun `deleteSelectedMangas should delete selected mangas and reload`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)
        viewModel.toggleMangaSelection(2L)
        viewModel.deleteSelectedMangas()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockDeleteComicsUseCase.deleteMultiple(listOf(1L, 2L)) }
        coVerify(atLeast = 2) { mockGetAllMangaUseCase() } // Initial + reload after delete

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse() // Should exit selection mode
        }

        viewModel.selectedMangas.test {
            assertThat(awaitItem()).isEmpty() // Should clear selection
        }
    }

    @Test
    fun `markSelectedAsFavorite should update favorite status and reload`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)
        viewModel.markSelectedAsFavorite(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUpdateMangaFavoriteStatusUseCase.updateMultiple(listOf(1L), true) }
        coVerify(atLeast = 2) { mockGetAllMangaUseCase() } // Initial + reload after update

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse() // Should exit selection mode
        }
    }

    @Test
    fun `markSelectedAsRead should mark mangas as read and reload`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)
        viewModel.markSelectedAsRead()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockMarkMangasAsReadUseCase.markMultipleAsRead(listOf(1L)) }
        coVerify(atLeast = 2) { mockGetAllMangaUseCase() } // Initial + reload after update

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse() // Should exit selection mode
        }
    }

    // === Refresh Tests ===

    @Test
    fun `refreshComics should reload manga list`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        clearMocks(mockGetAllMangaUseCase) // Clear previous calls
        coEvery { mockGetAllMangaUseCase() } returns flowOf(testMangaList)

        viewModel.refreshComics()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockGetAllMangaUseCase() }
    }

    // === Error Handling Tests ===

    @Test
    fun `importComic should handle import error gracefully`() = runTest {
        val exception = RuntimeException("Import failed")
        coEvery { mockImportComicsUseCase(any()) } throws exception

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.importComic("/invalid/path")
        testDispatcher.scheduler.advanceUntilIdle()

        // Should not crash and should still try to reload
        coVerify { mockImportComicsUseCase(File("/invalid/path")) }
    }

    @Test
    fun `batch operations should handle errors gracefully`() = runTest {
        val exception = RuntimeException("Operation failed")
        coEvery { mockDeleteComicsUseCase.deleteMultiple(any()) } throws exception

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterSelectionMode(1L)
        viewModel.deleteSelectedMangas()
        testDispatcher.scheduler.advanceUntilIdle()

        // Should handle error gracefully and clear selection
        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    // === Integration Tests ===

    @Test
    fun `search and sort should work together correctly`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Search for "manga" (should match "Test Manga 1")
        viewModel.searchComics("manga")
        // Sort by title descending
        viewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_DESC)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).hasSize(1)
            assertThat(comics.first().title).isEqualTo("Test Manga 1")
        }
    }

    @Test
    fun `state should persist through search and selection operations`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Search for comics
        viewModel.searchComics("test")
        testDispatcher.scheduler.advanceUntilIdle()

        // Enter selection mode
        viewModel.enterSelectionMode(1L)

        // Verify both search and selection states are maintained
        viewModel.searchQuery.test {
            assertThat(awaitItem()).isEqualTo("test")
        }

        viewModel.selectionMode.test {
            assertThat(awaitItem()).isTrue()
        }

        viewModel.selectedMangas.test {
            assertThat(awaitItem()).containsExactly(1L)
        }

        viewModel.getComics().test {
            val comics = awaitItem()
            assertThat(comics).hasSize(1) // Still filtered by search
            assertThat(comics.first().title).isEqualTo("Test Manga 1")
        }
    }
}
