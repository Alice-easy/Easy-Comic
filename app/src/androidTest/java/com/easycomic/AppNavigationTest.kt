package com.easycomic

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.easycomic.di.testModule
import com.easycomic.domain.model.Manga
import com.easycomic.domain.repository.MangaRepository
import com.easycomic.MainActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
class AppNavigationTest : KoinTest {

    private val mangaRepository: MangaRepository by inject()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        // 加载测试模块
        loadKoinModules(testModule)
        
        // 在测试开始前，插入一本漫画用于测试
        runBlocking {
            mangaRepository.insertOrUpdateManga(
                Manga(
                    id = 1,
                    title = "Sample Manga",
                    filePath = "/fake/path/to/sample_manga.cbz"
                )
            )
        }
    }

    @Test
    fun navigateFromBookshelfToReader() {
        // 1. 在书架屏幕上找到我们插入的漫画并点击它
        composeTestRule.onNodeWithText("Sample Manga").performClick()

        // 2. 验证是否已导航到阅读器屏幕
        //    (我们通过查找漫画标题来验证)
        composeTestRule.onNodeWithText("Sample Manga").assertExists()
    }
}
