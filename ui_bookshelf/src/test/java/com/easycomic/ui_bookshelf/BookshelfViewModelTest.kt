package com.easycomic.ui_bookshelf

import app.cash.turbine.test
import com.easycomic.data.repository.ComicImportRepositoryImpl
import com.easycomic.domain.model.Manga
import com.easycomic.domain.usecase.manga.*
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookshelfViewModelTest {

    private lateinit var viewModel: BookshelfViewModel
    private lateinit var getAllMangaUseCase: GetAllMangaUseCase
    private lateinit var importComicsUseCase: ImportComicsUseCase
    private lateinit var comicImportRepository: ComicImportRepositoryImpl
    private lateinit var deleteComicsUseCase: DeleteComicsUseCase
    private lateinit var updateMangaFavoriteStatusUseCase: UpdateMangaFavoriteStatusUseCase
    private lateinit var markMangasAsReadUseCase: MarkMangasAsReadUseCase

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        getAllMangaUseCase = mockk()
        importComicsUseCase = mockk()
        comicImportRepository = mockk()
        deleteComicsUseCase = mockk()
        updateMangaFavoriteStatusUseCase = mockk()
        markMangasAsReadUseCase = mockk()

        // Mock default behavior
        coEvery { getAllMangaUseCase() } returns flowOf(emptyList())
        
        viewModel = BookshelfViewModel(
            getAllMangaUseCase = getAllMangaUseCase,
            importComicsUseCase = importComicsUseCase,
            comicImportRepository = comicImportRepository,
            deleteComicsUseCase = deleteComicsUseCase,
            updateMangaFavoriteStatusUseCase = updateMangaFavoriteStatusUseCase,
            markMangasAsReadUseCase = markMangasAsReadUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `初始化时应该加载漫画列表`() = runTest {
        // Given
        val testMangas = listOf(
            createTestManga(1L, "Test Manga 1"),
            createTestManga(2L, "Test Manga 2")
        )
        coEvery { getAllMangaUseCase() } returns flowOf(testMangas)

        // When
        val newViewModel = BookshelfViewModel(
            getAllMangaUseCase = getAllMangaUseCase,
            importComicsUseCase = importComicsUseCase,
            comicImportRepository = comicImportRepository,
            deleteComicsUseCase = deleteComicsUseCase,
            updateMangaFavoriteStatusUseCase = updateMangaFavoriteStatusUseCase,
            markMangasAsReadUseCase = markMangasAsReadUseCase
        )

        // Then
        newViewModel.getComics().test {
            assertThat(awaitItem()).isEqualTo(testMangas)
        }
        
        newViewModel.isLoading.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `搜索功能应该正确过滤漫画`() = runTest {
        // Given
        val testMangas = listOf(
            createTestManga(1L, "Dragon Ball"),
            createTestManga(2L, "One Piece"),
            createTestManga(3L, "Naruto")
        )
        coEvery { getAllMangaUseCase() } returns flowOf(testMangas)

        val newViewModel = BookshelfViewModel(
            getAllMangaUseCase = getAllMangaUseCase,
            importComicsUseCase = importComicsUseCase,
            comicImportRepository = comicImportRepository,
            deleteComicsUseCase = deleteComicsUseCase,
            updateMangaFavoriteStatusUseCase = updateMangaFavoriteStatusUseCase,
            markMangasAsReadUseCase = markMangasAsReadUseCase
        )

        // When
        newViewModel.searchComics("Dragon")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        newViewModel.getComics().test {
            val filteredMangas = awaitItem()
            assertThat(filteredMangas).hasSize(1)
            assertThat(filteredMangas[0].title).isEqualTo("Dragon Ball")
        }
    }

    @Test
    fun `选择模式应该正确管理选中状态`() = runTest {
        // When
        viewModel.enterSelectionMode(1L)

        // Then
        viewModel.selectionMode.test {
            assertThat(awaitItem()).isTrue()
        }
        
        viewModel.selectedMangas.test {
            assertThat(awaitItem()).containsExactly(1L)
        }
    }

    @Test
    fun `删除选中漫画应该调用删除用例`() = runTest {
        // Given
        coEvery { deleteComicsUseCase.deleteMultiple(any()) } just Runs
        coEvery { getAllMangaUseCase() } returns flowOf(emptyList())
        
        viewModel.enterSelectionMode(1L)
        viewModel.toggleMangaSelection(2L)

        // When
        viewModel.deleteSelectedMangas()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { deleteComicsUseCase.deleteMultiple(listOf(1L, 2L)) }
        
        viewModel.selectionMode.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    private fun createTestManga(
        id: Long,
        title: String,
        author: String = "",
        currentPage: Int = 0,
        pageCount: Int = 100,
        dateAdded: Long = System.currentTimeMillis(),
        lastRead: Long = 0L
    ): Manga {
        return Manga(
            id = id,
            title = title,
            author = author,
            filePath = "/test/path/$title",
            coverImagePath = null,
            currentPage = currentPage,
            pageCount = pageCount,
            dateAdded = dateAdded,
            lastRead = lastRead,
            isFavorite = false
        )
    }
}