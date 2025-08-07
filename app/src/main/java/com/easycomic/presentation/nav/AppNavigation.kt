package com.easycomic.presentation.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.easycomic.presentation.ui.screens.BookshelfScreen
import com.easycomic.presentation.ui.screens.FavoritesScreen
import com.easycomic.presentation.ui.screens.ReaderScreen
import com.easycomic.presentation.ui.screens.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Bookshelf.route
    ) {
        composable(BottomNavItem.Bookshelf.route) {
            BookshelfScreen(
                onMangaClick = { mangaId ->
                    navController.navigate("reader/$mangaId")
                }
            )
        }
        
        composable(BottomNavItem.Favorites.route) {
            FavoritesScreen(
                onMangaClick = { mangaId ->
                    navController.navigate("reader/$mangaId")
                }
            )
        }
        
        composable(BottomNavItem.Settings.route) {
            SettingsScreen()
        }
        
        composable(
            route = "reader/{mangaId}",
            arguments = listOf(navArgument("mangaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mangaId = backStackEntry.arguments?.getLong("mangaId") ?: -1L
            ReaderScreen(
                mangaId = mangaId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}