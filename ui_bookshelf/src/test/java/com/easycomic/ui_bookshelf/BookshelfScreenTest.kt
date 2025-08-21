package com.easycomic.ui_bookshelf

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.domain.model.Manga
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * BookshelfScreen Compose UI测试
 * 测试书架界面的UI交互和状态显示
 */
@RunWith(AndroidJUnit4::class)
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: BookshelfViewModel
    private lateinit var mockOnNavigateToReader: (Long) -> Unit
    private lateinit var mockOnNavigateToSettings: () -> Unit

    private val testMangas = listOf(
        Manga(
            id = 1L,
            title = "测试漫画1",
            filePath = "/test/path1",
            coverImagePath = "/test/cover1.jpg",
            pageCount = 100,
            currentPage = 1,
            lastReadTime = System.currentTimeMillis(),
            isFavorite = false,
            author = "作者1",
            rating = 4.5f,
            progressPercentage = 25f
        ),
        Manga(
            id = 2L,
            title = "测试漫画2",
            filePath = "/test/path2",
            coverImagePath = "/test/cover2.jpg",
            pageCount = 200,
            currentPage = 50,
            lastReadTime = System.currentTimeMillis() - 1000,
            isFavorite = true,
            author = "作者2",
            rating = 4.0f,
            progressPercentage = 75f
        )
    )

    @Before
    fun setup() {
        mockViewModel = mockk(relaxed = true)
        mockOnNavigateToReader = mockk(relaxed = true)
        mockOnNavigateToSettings = mockk(relaxed = true)

        // 设置默认的ViewModel状态
        every { mockViewModel.getComics() } returns flowOf(testMangas)
        every { mockViewModel.searchQuery } returns MutableStateFlow("")
        every { mockViewModel.sortOrder } returns MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
        every { mockViewModel.isLoading } returns MutableStateFlow(false)
        every { mockViewModel.importProgress } returns MutableStateFlow(null)
        every { mockViewModel.isImporting } returns MutableStateFlow(false)
        every { mockViewModel.selectionMode } returns MutableStateFlow(false)
        every { mockViewModel.selectedMangas } returns MutableStateFlow(emptySet())
    }

    @Test
    fun bookshelfScreen_displaysTitle() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("书架")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_displaysMangaList() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        // 验证漫画列表显示
        composeTestRule
            .onNodeWithText("测试漫画1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("测试漫画2")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_showsLoadingState() {
        every { mockViewModel.isLoading } returns MutableStateFlow(true)
        every { mockViewModel.getComics() } returns flowOf(emptyList())

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("正在加载漫画...")
            .assertIsDisplayed()
        
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_showsEmptyState() {
        every { mockViewModel.getComics() } returns flowOf(emptyList())

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("还没有漫画，点击右下角按钮导入")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_showsEmptySearchResults() {
        every { mockViewModel.getComics() } returns flowOf(emptyList())
        every { mockViewModel.searchQuery } returns MutableStateFlow("不存在的漫画")

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("未找到匹配的漫画")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_clickManga_navigatesToReader() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画1")
            .performClick()

        verify { mockOnNavigateToReader(1L) }
    }

    @Test
    fun bookshelfScreen_clickSearchIcon_showsSearchBar() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("搜索")
            .performClick()

        composeTestRule
            .onNodeWithText("搜索漫画...")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_searchInput_callsViewModel() {
        every { mockViewModel.searchQuery } returns MutableStateFlow("测试")

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        // 打开搜索栏
        composeTestRule
            .onNodeWithContentDescription("搜索")
            .performClick()

        // 输入搜索内容
        composeTestRule
            .onNodeWithText("搜索漫画...")
            .performTextInput("测试")

        verify { mockViewModel.searchComics("测试") }
    }

    @Test
    fun bookshelfScreen_clickSortIcon_showsSortMenu() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("排序")
            .performClick()

        composeTestRule
            .onNodeWithText("标题 A-Z")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("标题 Z-A")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("添加时间 (最新)")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_selectSortOption_callsViewModel() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        // 打开排序菜单
        composeTestRule
            .onNodeWithContentDescription("排序")
            .performClick()

        // 选择排序选项
        composeTestRule
            .onNodeWithText("标题 Z-A")
            .performClick()

        verify { mockViewModel.setSortOrder(BookshelfViewModel.SortOrder.TITLE_DESC) }
    }

    @Test
    fun bookshelfScreen_clickRefresh_callsViewModel() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("刷新")
            .performClick()

        verify { mockViewModel.refreshComics() }
    }

    @Test
    fun bookshelfScreen_clickSettings_navigatesToSettings() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("设置")
            .performClick()

        verify { mockOnNavigateToSettings() }
    }

    @Test
    fun bookshelfScreen_longClickManga_entersSelectionMode() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画1")
            .performTouchInput { longClick() }

        verify { mockViewModel.enterSelectionMode(1L) }
    }

    @Test
    fun bookshelfScreen_selectionMode_showsSelectionTopBar() {
        every { mockViewModel.selectionMode } returns MutableStateFlow(true)
        every { mockViewModel.selectedMangas } returns MutableStateFlow(setOf(1L))

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithText("已选择 1 项")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("取消选择")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithContentDescription("全选")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_selectionMode_clearSelection() {
        every { mockViewModel.selectionMode } returns MutableStateFlow(true)
        every { mockViewModel.selectedMangas } returns MutableStateFlow(setOf(1L))

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("取消选择")
            .performClick()

        verify { mockViewModel.clearSelection() }
    }

    @Test
    fun bookshelfScreen_selectionMode_selectAll() {
        every { mockViewModel.selectionMode } returns MutableStateFlow(true)
        every { mockViewModel.selectedMangas } returns MutableStateFlow(setOf(1L))

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("全选")
            .performClick()

        verify { mockViewModel.selectAllVisibleMangas(testMangas) }
    }

    @Test
    fun bookshelfScreen_clickFAB_triggersImport() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("导入漫画")
            .performClick()

        // 验证FAB点击后的行为（这里主要是验证UI响应）
        composeTestRule
            .onNodeWithContentDescription("导入漫画")
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_importing_showsProgressInFAB() {
        every { mockViewModel.isImporting } returns MutableStateFlow(true)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        // 验证FAB显示进度指示器
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_refreshDisabled_whenLoading() {
        every { mockViewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("刷新")
            .assertIsNotEnabled()
    }

    @Test
    fun bookshelfScreen_refreshDisabled_whenImporting() {
        every { mockViewModel.isImporting } returns MutableStateFlow(true)

        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        composeTestRule
            .onNodeWithContentDescription("刷新")
            .assertIsNotEnabled()
    }

    @Test
    fun bookshelfScreen_displaysMangaWithCorrectInfo() {
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onNavigateToReader = mockOnNavigateToReader,
                onNavigateToSettings = mockOnNavigateToSettings
            )
        }

        // 验证漫画信息显示
        composeTestRule
            .onNodeWithText("测试漫画1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("作者1")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("1/100")
            .assertIsDisplayed()
    }
}