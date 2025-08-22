package com.easycomic.ui_bookshelf

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.domain.model.Manga
import com.easycomic.domain.model.ReadingStatus
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock ViewModel
    private val mockViewModel = mockk<BookshelfViewModel>(relaxed = true)

    // StateFlow mocks for ViewModel properties
    private val comicsFlow = MutableStateFlow<List<Manga>>(emptyList())
    private val searchQueryFlow = MutableStateFlow("")
    private val sortOrderFlow = MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
    private val isLoadingFlow = MutableStateFlow(false)
    private val importProgressFlow = MutableStateFlow<com.easycomic.data.repository.ImportProgress?>(null)
    private val isImportingFlow = MutableStateFlow(false)
    private val selectionModeFlow = MutableStateFlow(false)
    private val selectedMangasFlow = MutableStateFlow<Set<Long>>(emptySet())

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
        // Setup ViewModel mock to return StateFlows
        every { mockViewModel.getComics() } returns comicsFlow
        every { mockViewModel.searchQuery } returns searchQueryFlow
        every { mockViewModel.sortOrder } returns sortOrderFlow
        every { mockViewModel.isLoading } returns isLoadingFlow
        every { mockViewModel.importProgress } returns importProgressFlow
        every { mockViewModel.isImporting } returns isImportingFlow
        every { mockViewModel.selectionMode } returns selectionModeFlow
        every { mockViewModel.selectedMangas } returns selectedMangasFlow

        // Setup default method behaviors
        every { mockViewModel.searchComics(any()) } returns Unit
        every { mockViewModel.setSortOrder(any()) } returns Unit
        every { mockViewModel.enterSelectionMode(any()) } returns Unit
        every { mockViewModel.clearSelection() } returns Unit
        every { mockViewModel.toggleMangaSelection(any()) } returns Unit
        every { mockViewModel.deleteSelectedMangas() } returns Unit
        every { mockViewModel.markSelectedAsFavorite(any()) } returns Unit
        every { mockViewModel.markSelectedAsRead() } returns Unit
        every { mockViewModel.clearImportProgress() } returns Unit
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // === Display Tests ===

    @Test
    fun bookshelfScreen_whenLoading_showsLoadingIndicator() {
        isLoadingFlow.value = true

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Loading comics")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_whenEmpty_showsEmptyState() {
        comicsFlow.value = emptyList()
        isLoadingFlow.value = false

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("没有漫画")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("点击右上角 + 按钮导入漫画")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_withMangas_displaysComicGrid() {
        comicsFlow.value = testMangaList
        isLoadingFlow.value = false

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Check that manga titles are displayed
        composeTestRule
            .onNodeWithText("Test Manga 1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Another Comic")
            .assertIsDisplayed()

        // Check that authors are displayed
        composeTestRule
            .onNodeWithText("Author 1")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Author 2")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_clickOnManga_navigatesToReader() {
        comicsFlow.value = testMangaList
        isLoadingFlow.value = false
        var navigatedToMangaId: Long? = null

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { mangaId -> 
                    navigatedToMangaId = mangaId 
                }
            )
        }

        composeTestRule
            .onNodeWithText("Test Manga 1")
            .performClick()

        assert(navigatedToMangaId == 1L)
    }

    // === Search Functionality Tests ===

    @Test
    fun bookshelfScreen_clickSearchIcon_showsSearchBar() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Search comics")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索漫画...")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_enterSearchQuery_callsViewModelSearch() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Open search
        composeTestRule
            .onNodeWithContentDescription("Search comics")
            .performClick()

        // Enter search text
        composeTestRule
            .onNodeWithText("搜索漫画...")
            .performTextInput("test query")

        verify { mockViewModel.searchComics("test query") }
    }

    @Test
    fun bookshelfScreen_clearSearch_callsViewModelWithEmptyQuery() {
        comicsFlow.value = testMangaList
        searchQueryFlow.value = "existing query"

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Open search (should already be open with existing query)
        composeTestRule
            .onNodeWithContentDescription("Clear search")
            .performClick()

        verify { mockViewModel.searchComics("") }
    }

    // === Sort Functionality Tests ===

    @Test
    fun bookshelfScreen_clickSortIcon_showsSortMenu() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Sort comics")
            .performClick()

        // Check that sort options are displayed
        composeTestRule
            .onNodeWithText("标题 (A-Z)")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("标题 (Z-A)")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("添加时间 (最新)")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_selectSortOption_callsViewModelSetSortOrder() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Open sort menu
        composeTestRule
            .onNodeWithContentDescription("Sort comics")
            .performClick()

        // Select title descending
        composeTestRule
            .onNodeWithText("标题 (Z-A)")
            .performClick()

        verify { mockViewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_DESC) }
    }

    // === Selection Mode Tests ===

    @Test
    fun bookshelfScreen_longClickOnManga_entersSelectionMode() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("Test Manga 1")
            .performTouchInput { longClick() }

        verify { mockViewModel.enterSelectionMode(1L) }
    }

    @Test
    fun bookshelfScreen_inSelectionMode_showsBatchActions() {
        comicsFlow.value = testMangaList
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Should show back arrow to exit selection
        composeTestRule
            .onNodeWithContentDescription("Exit selection mode")
            .assertIsDisplayed()

        // Should show selected count
        composeTestRule
            .onNodeWithText("已选择 1 项")
            .assertIsDisplayed()

        // Should show batch action menu
        composeTestRule
            .onNodeWithContentDescription("Batch actions")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_clickExitSelection_callsViewModelClearSelection() {
        comicsFlow.value = testMangaList
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Exit selection mode")
            .performClick()

        verify { mockViewModel.clearSelection() }
    }

    @Test
    fun bookshelfScreen_clickBatchActions_showsBatchMenu() {
        comicsFlow.value = testMangaList
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L, 2L)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithContentDescription("Batch actions")
            .performClick()

        // Check batch action options
        composeTestRule
            .onNodeWithText("删除")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("标记为收藏")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("标记为已读")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_selectBatchDelete_callsViewModelDeleteSelected() {
        comicsFlow.value = testMangaList
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L, 2L)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Open batch menu
        composeTestRule
            .onNodeWithContentDescription("Batch actions")
            .performClick()

        // Select delete
        composeTestRule
            .onNodeWithText("删除")
            .performClick()

        verify { mockViewModel.deleteSelectedMangas() }
    }

    @Test
    fun bookshelfScreen_selectBatchFavorite_callsViewModelMarkAsFavorite() {
        comicsFlow.value = testMangaList
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Open batch menu
        composeTestRule
            .onNodeWithContentDescription("Batch actions")
            .performClick()

        // Select mark as favorite
        composeTestRule
            .onNodeWithText("标记为收藏")
            .performClick()

        verify { mockViewModel.markSelectedAsFavorite(true) }
    }

    // === Import Progress Tests ===

    @Test
    fun bookshelfScreen_whenImporting_showsImportProgress() {
        comicsFlow.value = testMangaList
        isImportingFlow.value = true
        importProgressFlow.value = com.easycomic.data.repository.ImportProgress.Scanning

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("正在扫描文件...")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_importProcessing_showsProgressWithFileName() {
        comicsFlow.value = testMangaList
        isImportingFlow.value = true
        importProgressFlow.value = com.easycomic.data.repository.ImportProgress.Processing(
            fileName = "manga1.zip",
            current = 1,
            total = 3
        )

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("正在处理: manga1.zip (1/3)")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_importCompleted_showsCompletionMessage() {
        comicsFlow.value = testMangaList
        isImportingFlow.value = false
        importProgressFlow.value = com.easycomic.data.repository.ImportProgress.Completed(
            successCount = 5,
            failureCount = 1
        )

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("导入完成：成功 5 个，失败 1 个")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("确定")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_importError_showsErrorMessage() {
        comicsFlow.value = testMangaList
        isImportingFlow.value = false
        importProgressFlow.value = com.easycomic.data.repository.ImportProgress.Error("导入失败")

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("导入失败")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("确定")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_dismissImportDialog_callsViewModelClearProgress() {
        comicsFlow.value = testMangaList
        importProgressFlow.value = com.easycomic.data.repository.ImportProgress.Completed(2, 0)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        composeTestRule
            .onNodeWithText("确定")
            .performClick()

        verify { mockViewModel.clearImportProgress() }
    }

    // === Accessibility Tests ===

    @Test
    fun bookshelfScreen_hasProperContentDescriptions() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // Check important UI elements have content descriptions
        composeTestRule
            .onNodeWithContentDescription("Add comics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Search comics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Sort comics")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
    }

    // === Integration Tests ===

    @Test
    fun bookshelfScreen_complexUserWorkflow_worksCorrectly() {
        comicsFlow.value = testMangaList

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = { }
            )
        }

        // 1. Search for comics
        composeTestRule
            .onNodeWithContentDescription("Search comics")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索漫画...")
            .performTextInput("test")

        verify { mockViewModel.searchComics("test") }

        // 2. Change sort order
        composeTestRule
            .onNodeWithContentDescription("Sort comics")
            .performClick()

        composeTestRule
            .onNodeWithText("标题 (Z-A)")
            .performClick()

        verify { mockViewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_DESC) }

        // 3. Enter selection mode
        composeTestRule
            .onNodeWithText("Test Manga 1")
            .performTouchInput { longClick() }

        verify { mockViewModel.enterSelectionMode(1L) }

        // Simulate selection mode state change
        selectionModeFlow.value = true
        selectedMangasFlow.value = setOf(1L)

        // 4. Add more items to selection
        composeTestRule
            .onNodeWithText("Another Comic")
            .performClick()

        verify { mockViewModel.toggleMangaSelection(2L) }

        // 5. Perform batch action
        composeTestRule
            .onNodeWithContentDescription("Batch actions")
            .performClick()

        composeTestRule
            .onNodeWithText("标记为收藏")
            .performClick()

        verify { mockViewModel.markSelectedAsFavorite(true) }
    }
}
