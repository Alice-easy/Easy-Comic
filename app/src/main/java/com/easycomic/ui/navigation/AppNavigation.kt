package com.easycomic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.easycomic.ui.bookshelf.BookshelfScreen
import com.easycomic.ui.bookshelf.BookshelfViewModel
import com.easycomic.ui.reader.ReaderScreen
import com.easycomic.ui.reader.ReaderViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * 导航控制器
 */
sealed class Screen(val route: String) {
    object Bookshelf : Screen("bookshelf")
    object Reader : Screen("reader/{mangaId}") {
        fun createRoute(mangaId: Long) = "reader/$mangaId"
    }
}

/**
 * 应用导航图
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Bookshelf.route
    ) {
        composable(Screen.Bookshelf.route) {
            val bookshelfViewModel: BookshelfViewModel = koinViewModel()
            
            BookshelfScreen(
                viewModel = bookshelfViewModel,
                navController = navController,
                onMangaClick = { manga ->
                    navController.navigate(Screen.Reader.createRoute(manga.id))
                }
            )
        }
        
        composable(
            route = Screen.Reader.route,
            arguments = listOf(
                navArgument("mangaId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getLong("mangaId") ?: 0L
            val readerViewModel: ReaderViewModel = koinViewModel()
            
            ReaderScreen(
                viewModel = readerViewModel,
                filePath = "", // This will be handled by the ViewModel
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}