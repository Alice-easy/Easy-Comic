package com.easycomic.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.easycomic.ui.navigation.Screen
import com.easycomic.ui.theme.EasyComicTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 导航测试
 */
@RunWith(AndroidJUnit4::class)
@MediumTest
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `when starting app, should show bookshelf screen`() {
        // Given
        composeTestRule.setContent {
            EasyComicTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable(Screen.Reader.route) {
                        androidx.compose.material3.Text("阅读器界面")
                    }
                }
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("书架界面")
            .assertIsDisplayed()
    }

    @Test
    fun `when navigating to reader, should show reader screen`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable(Screen.Reader.route) {
                        androidx.compose.material3.Text("阅读器界面")
                    }
                }
            }
        }

        // When - 导航到阅读器界面
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Reader.route)
        }

        // Then
        composeTestRule
            .onNodeWithText("阅读器界面")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("书架界面")
            .assertDoesNotExist()
    }

    @Test
    fun `when navigating back from reader, should return to bookshelf`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable(Screen.Reader.route) {
                        androidx.compose.material3.Text("阅读器界面")
                    }
                }
            }
        }

        // When - 导航到阅读器界面然后返回
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Reader.route)
        }
        
        composeTestRule.runOnUiThread {
            navController.popBackStack()
        }

        // Then
        composeTestRule
            .onNodeWithText("书架界面")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("阅读器界面")
            .assertDoesNotExist()
    }

    @Test
    fun `when navigating with arguments, should handle correctly`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable("reader/{mangaId}") { backStackEntry ->
                        val mangaId = backStackEntry.arguments?.getString("mangaId")
                        androidx.compose.material3.Text("阅读器界面 - 漫画ID: $mangaId")
                    }
                }
            }
        }

        // When - 导航到阅读器界面并传递参数
        composeTestRule.runOnUiThread {
            navController.navigate("reader/123")
        }

        // Then
        composeTestRule
            .onNodeWithText("阅读器界面 - 漫画ID: 123")
            .assertIsDisplayed()
    }

    @Test
    fun `when navigating to same destination, should handle gracefully`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable(Screen.Reader.route) {
                        androidx.compose.material3.Text("阅读器界面")
                    }
                }
            }
        }

        // When - 多次导航到同一目的地
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Reader.route)
        }
        
        composeTestRule.runOnUiThread {
            navController.navigate(Screen.Reader.route)
        }

        // Then - 应该仍然显示阅读器界面
        composeTestRule
            .onNodeWithText("阅读器界面")
            .assertIsDisplayed()
    }

    @Test
    fun `when popping up to specific destination, should work correctly`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                    composable("screen1") {
                        androidx.compose.material3.Text("界面1")
                    }
                    composable("screen2") {
                        androidx.compose.material3.Text("界面2")
                    }
                    composable("screen3") {
                        androidx.compose.material3.Text("界面3")
                    }
                }
            }
        }

        // When - 构建导航栈并弹出到指定目的地
        composeTestRule.runOnUiThread {
            navController.navigate("screen1")
            navController.navigate("screen2")
            navController.navigate("screen3")
        }
        
        composeTestRule.runOnUiThread {
            navController.popBackStack("screen1", inclusive = false)
        }

        // Then - 应该显示界面2
        composeTestRule
            .onNodeWithText("界面2")
            .assertIsDisplayed()
    }

    @Test
    fun `when navigation stack is empty, should handle gracefully`() {
        // Given
        lateinit var navController: androidx.navigation.NavController
        
        composeTestRule.setContent {
            EasyComicTheme {
                navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = Screen.Bookshelf.route
                ) {
                    composable(Screen.Bookshelf.route) {
                        androidx.compose.material3.Text("书架界面")
                    }
                }
            }
        }

        // When - 尝试从根界面返回
        val canPop = navController.popBackStack()

        // Then - 应该无法弹出
        assert(!canPop)
        composeTestRule
            .onNodeWithText("书架界面")
            .assertIsDisplayed()
    }
}