package com.easycomic.ui_bookshelf

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.domain.model.Manga
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookshelfScreen_显示漫画列表() {
        // Given
        val testMangas = listOf(
            createTestManga(1L, "Test Manga 1"),
            createTestManga(2L, "Test Manga 2")
        )
        
        val mockViewModel = mockk<BookshelfViewModel>(relaxed = true) {
            every { getComics() } returns MutableStateFlow(testMangas)
            every { isLoading } returns MutableStateFlow(false)
            every { searchQuery } returns MutableStateFlow("")
            every { selectionMode } returns MutableStateFlow(false)
            every { selectedMangas } returns MutableStateFlow(emptySet())
            every { sortOrder } returns MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
        }

        // When
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onMangaClick = { },
                onImportClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test Manga 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Manga 2").assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_显示加载状态() {
        // Given
        val mockViewModel = mockk<BookshelfViewModel>(relaxed = true) {
            every { getComics() } returns MutableStateFlow(emptyList())
            every { isLoading } returns MutableStateFlow(true)
            every { searchQuery } returns MutableStateFlow("")
            every { selectionMode } returns MutableStateFlow(false)
            every { selectedMangas } returns MutableStateFlow(emptySet())
            every { sortOrder } returns MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
        }

        // When
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onMangaClick = { },
                onImportClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_显示空状态() {
        // Given
        val mockViewModel = mockk<BookshelfViewModel>(relaxed = true) {
            every { getComics() } returns MutableStateFlow(emptyList())
            every { isLoading } returns MutableStateFlow(false)
            every { searchQuery } returns MutableStateFlow("")
            every { selectionMode } returns MutableStateFlow(false)
            every { selectedMangas } returns MutableStateFlow(emptySet())
            every { sortOrder } returns MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
        }

        // When
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onMangaClick = { },
                onImportClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithText("暂无漫画").assertIsDisplayed()
    }

    @Test
    fun bookshelfScreen_搜索功能() {
        // Given
        val mockViewModel = mockk<BookshelfViewModel>(relaxed = true) {
            every { getComics() } returns MutableStateFlow(emptyList())
            every { isLoading } returns MutableStateFlow(false)
            every { searchQuery } returns MutableStateFlow("")
            every { selectionMode } returns MutableStateFlow(false)
            every { selectedMangas } returns MutableStateFlow(emptySet())
            every { sortOrder } returns MutableStateFlow(BookshelfViewModel.SortOrder.TITLE_ASC)
        }

        // When
        composeTestRule.setContent {
            BookshelfScreen(
                viewModel = mockViewModel,
                onMangaClick = { },
                onImportClick = { }
            )
        }

        // Then
        composeTestRule.onNodeWithTag("search_field").assertIsDisplayed()
        
        // 输入搜索文本
        composeTestRule.onNodeWithTag("search_field").performTextInput("test")
        
        // 验证搜索功能被调用
        // 注意：这里需要验证 viewModel.searchComics 被调用，但由于是 mockk，需要额外的验证逻辑
    }

    private fun createTestManga(
        id: Long,
        title: String,
        author: String? = null,
        currentPage: Int = 0,
        pageCount: Int = 100
    ): Manga {
        return Manga(
            id = id,
            title = title,
            author = author,
            filePath = "/test/path/$title",
            coverImagePath = null,
            currentPage = currentPage,
            pageCount = pageCount,
            dateAdded = System.currentTimeMillis(),
            lastRead = null,
            isFavorite = false
        )
    }
}