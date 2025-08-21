package com.easycomic.ui_reader

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.domain.model.Manga
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ReaderScreen Compose UI测试
 * 测试漫画阅读器屏幕的UI显示和交互
 */
@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockOnPageChange: (Int) -> Unit
    private lateinit var mockOnSettingsClick: () -> Unit
    private lateinit var mockOnBackClick: () -> Unit
    private lateinit var mockOnBookmarkClick: () -> Unit

    private val testManga = Manga(
        id = 1L,
        title = "测试漫画",
        filePath = "/test/path",
        coverImagePath = "/test/cover.jpg",
        pageCount = 50,
        currentPage = 10,
        lastReadTime = System.currentTimeMillis(),
        isFavorite = false,
        author = "测试作者",
        rating = 4.0f,
        progressPercentage = 20f
    )

    @Before
    fun setup() {
        mockOnPageChange = mockk(relaxed = true)
        mockOnSettingsClick = mockk(relaxed = true)
        mockOnBackClick = mockk(relaxed = true)
        mockOnBookmarkClick = mockk(relaxed = true)
    }

    @Test
    fun readerScreen_displaysMangaTitle() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_displaysPageInfo() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithText("10 / 50")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_showsProgressBar() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 验证进度条存在 (10/50 = 0.2)
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.2f, 0f..1f)))
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_backButton_triggersCallback() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("返回")
            .performClick()

        verify { mockOnBackClick() }
    }

    @Test
    fun readerScreen_settingsButton_triggersCallback() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("设置")
            .performClick()

        verify { mockOnSettingsClick() }
    }

    @Test
    fun readerScreen_bookmarkButton_triggersCallback() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("书签")
            .performClick()

        verify { mockOnBookmarkClick() }
    }

    @Test
    fun readerScreen_swipeLeft_goesToNextPage() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 模拟向左滑动手势
        composeTestRule
            .onNodeWithTag("reader_content")
            .performTouchInput {
                swipeLeft()
            }

        verify { mockOnPageChange(11) }
    }

    @Test
    fun readerScreen_swipeRight_goesToPreviousPage() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 模拟向右滑动手势
        composeTestRule
            .onNodeWithTag("reader_content")
            .performTouchInput {
                swipeRight()
            }

        verify { mockOnPageChange(9) }
    }

    @Test
    fun readerScreen_firstPage_swipeRightDoesNothing() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 1,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithTag("reader_content")
            .performTouchInput {
                swipeRight()
            }

        verify(exactly = 0) { mockOnPageChange(any()) }
    }

    @Test
    fun readerScreen_lastPage_swipeLeftDoesNothing() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 50,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithTag("reader_content")
            .performTouchInput {
                swipeLeft()
            }

        verify(exactly = 0) { mockOnPageChange(any()) }
    }

    @Test
    fun readerScreen_tapCenter_togglesUI() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 点击中心区域隐藏UI
        composeTestRule
            .onNodeWithTag("reader_content")
            .performClick()

        // 验证标题栏隐藏
        composeTestRule
            .onNodeWithText("测试漫画")
            .assertIsNotDisplayed()

        // 再次点击显示UI
        composeTestRule
            .onNodeWithTag("reader_content")
            .performClick()

        // 验证标题栏显示
        composeTestRule
            .onNodeWithText("测试漫画")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_zoomGesture_zoomsImage() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 模拟缩放手势
        composeTestRule
            .onNodeWithTag("reader_image")
            .performTouchInput {
                // 双指缩放手势
                val center = this.center
                val finger1Start = center.copy(x = center.x - 100f)
                val finger2Start = center.copy(x = center.x + 100f)
                val finger1End = center.copy(x = center.x - 200f)
                val finger2End = center.copy(x = center.x + 200f)

                down(0, finger1Start)
                down(1, finger2Start)
                moveTo(0, finger1End)
                moveTo(1, finger2End)
                up(0)
                up(1)
            }

        // 验证图片仍然显示（缩放状态通过Matrix变换实现，难以直接测试）
        composeTestRule
            .onNodeWithTag("reader_image")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_loading_showsProgressIndicator() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                isLoading = true,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithTag("loading_indicator")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_error_showsErrorMessage() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                errorMessage = "加载失败",
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        composeTestRule
            .onNodeWithText("加载失败")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_brightness_adjustsCorrectly() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                brightness = 0.8f,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 验证亮度调节器存在
        composeTestRule
            .onNodeWithTag("brightness_slider")
            .assertIsDisplayed()
    }

    @Test
    fun readerScreen_fullscreen_hidesSystemUI() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 10,
                totalPages = 50,
                isFullscreen = true,
                onPageChange = mockOnPageChange,
                onSettingsClick = mockOnSettingsClick,
                onBackClick = mockOnBackClick,
                onBookmarkClick = mockOnBookmarkClick
            )
        }

        // 在全屏模式下，UI控件应该隐藏
        composeTestRule
            .onNodeWithText("测试漫画")
            .assertIsNotDisplayed()
    }
}