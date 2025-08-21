package com.easycomic.ui_bookshelf

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
 * ComicCard Compose UI测试
 * 测试漫画卡片组件的UI显示和交互
 */
@RunWith(AndroidJUnit4::class)
class ComicCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockOnClick: () -> Unit
    private lateinit var mockOnLongClick: () -> Unit
    private lateinit var mockOnFavoriteClick: () -> Unit

    private val testManga = Manga(
        id = 1L,
        title = "测试漫画标题",
        filePath = "/test/path",
        coverImagePath = "/test/cover.jpg",
        pageCount = 100,
        currentPage = 25,
        lastReadTime = System.currentTimeMillis(),
        isFavorite = false,
        author = "测试作者",
        rating = 4.5f,
        progressPercentage = 25f
    )

    private val favoriteManga = testManga.copy(
        id = 2L,
        title = "收藏漫画",
        isFavorite = true
    )

    @Before
    fun setup() {
        mockOnClick = mockk(relaxed = true)
        mockOnLongClick = mockk(relaxed = true)
        mockOnFavoriteClick = mockk(relaxed = true)
    }

    @Test
    fun comicCard_displaysMangaTitle() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_displaysMangaAuthor() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试作者")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_displaysPageInfo() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("25/100")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_displaysRating() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("4.5")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_showsFavoriteIcon_whenFavorite() {
        composeTestRule.setContent {
            ComicCard(
                manga = favoriteManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("收藏")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_hidesFavoriteIcon_whenNotFavorite() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("收藏")
            .assertDoesNotExist()
    }

    @Test
    fun comicCard_showsProgressIndicator_whenInProgress() {
        val mangaInProgress = testManga.copy(progressPercentage = 50f)

        composeTestRule.setContent {
            ComicCard(
                manga = mangaInProgress,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证进度条存在
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.5f, 0f..1f)))
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_hidesProgressIndicator_whenNotStarted() {
        val mangaNotStarted = testManga.copy(progressPercentage = 0f)

        composeTestRule.setContent {
            ComicCard(
                manga = mangaNotStarted,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证进度条不存在
        composeTestRule
            .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo(0f, 0f..1f)))
            .assertCountEquals(0)
    }

    @Test
    fun comicCard_hidesProgressIndicator_whenCompleted() {
        val mangaCompleted = testManga.copy(progressPercentage = 100f)

        composeTestRule.setContent {
            ComicCard(
                manga = mangaCompleted,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证进度条不存在
        composeTestRule
            .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo(1f, 0f..1f)))
            .assertCountEquals(0)
    }

    @Test
    fun comicCard_onClick_triggersCallback() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performClick()

        verify { mockOnClick() }
    }

    @Test
    fun comicCard_onLongClick_triggersCallback() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performTouchInput { longClick() }

        verify { mockOnLongClick() }
    }

    @Test
    fun comicCard_selectionMode_showsCheckbox() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                selectionMode = true,
                isSelected = false,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNode(hasClickAction() and hasStateDescription("未选中"))
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_selectionMode_showsSelectedCheckbox() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                selectionMode = true,
                isSelected = true,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNode(hasClickAction() and hasStateDescription("已选中"))
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_selectionMode_clickTriggersLongClick() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                selectionMode = true,
                isSelected = false,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performClick()

        verify { mockOnLongClick() }
        verify(exactly = 0) { mockOnClick() }
    }

    @Test
    fun comicCard_highlightsSearchText() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                searchQuery = "测试",
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证标题仍然显示（高亮功能通过AnnotatedString实现，难以直接测试高亮效果）
        composeTestRule
            .onNodeWithText("测试漫画标题")
            .assertIsDisplayed()
    }

    @Test
    fun comicCard_highlightsSearchTextInAuthor() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                searchQuery = "作者",
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证作者仍然显示
        composeTestRule
            .onNodeWithText("测试作者")
            .assertIsDisplayed()
    }

    @Test
    fun gridComicCard_displaysMangaTitle() {
        composeTestRule.setContent {
            GridComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .assertIsDisplayed()
    }

    @Test
    fun gridComicCard_showsFavoriteIcon_whenFavorite() {
        composeTestRule.setContent {
            GridComicCard(
                manga = favoriteManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithContentDescription("收藏")
            .assertIsDisplayed()
    }

    @Test
    fun gridComicCard_showsProgressIndicator_whenInProgress() {
        val mangaInProgress = testManga.copy(progressPercentage = 75f)

        composeTestRule.setContent {
            GridComicCard(
                manga = mangaInProgress,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证进度条存在
        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.75f, 0f..1f)))
            .assertIsDisplayed()
    }

    @Test
    fun gridComicCard_selectionMode_showsCheckbox() {
        composeTestRule.setContent {
            GridComicCard(
                manga = testManga,
                selectionMode = true,
                isSelected = true,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNode(hasClickAction() and hasStateDescription("已选中"))
            .assertIsDisplayed()
    }

    @Test
    fun gridComicCard_onClick_triggersCallback() {
        composeTestRule.setContent {
            GridComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performClick()

        verify { mockOnClick() }
    }

    @Test
    fun gridComicCard_onLongClick_triggersCallback() {
        composeTestRule.setContent {
            GridComicCard(
                manga = testManga,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performTouchInput { longClick() }

        verify { mockOnLongClick() }
    }

    @Test
    fun gridComicCard_selectionMode_clickTriggersLongClick() {
        composeTestRule.setContent {
            GridComicCard(
                manga = testManga,
                selectionMode = true,
                isSelected = false,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试漫画标题")
            .performClick()

        verify { mockOnLongClick() }
        verify(exactly = 0) { mockOnClick() }
    }

    @Test
    fun comicCard_withoutAuthor_hidesAuthorText() {
        val mangaWithoutAuthor = testManga.copy(author = "")

        composeTestRule.setContent {
            ComicCard(
                manga = mangaWithoutAuthor,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("测试作者")
            .assertDoesNotExist()
    }

    @Test
    fun comicCard_withoutRating_hidesRatingInfo() {
        val mangaWithoutRating = testManga.copy(rating = 0f)

        composeTestRule.setContent {
            ComicCard(
                manga = mangaWithoutRating,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        composeTestRule
            .onNodeWithText("4.5")
            .assertDoesNotExist()
    }

    @Test
    fun comicCard_selectedState_showsElevatedCard() {
        composeTestRule.setContent {
            ComicCard(
                manga = testManga,
                isSelected = true,
                onClick = mockOnClick,
                onLongClick = mockOnLongClick
            )
        }

        // 验证卡片仍然显示（选中状态通过elevation和color变化体现，难以直接测试）
        composeTestRule
            .onNodeWithText("测试漫画标题")
            .assertIsDisplayed()
    }
}