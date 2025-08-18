package com.easycomic.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.easycomic.ui_bookshelf.BookshelfScreen
import com.easycomic.ui_reader.ReaderScreen
import com.easycomic.ui_reader.ReaderViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "bookshelf") {
        composable("bookshelf") {
            BookshelfScreen(
                onNavigateToReader = { mangaId ->
                    navController.navigate("reader/$mangaId")
                }
            )
        }
        composable(
            route = "reader/{mangaId}",
            arguments = listOf(navArgument("mangaId") { type = NavType.LongType })
        ) {
            // The viewModel is created by the framework with the SavedStateHandle,
            // so we don't need to manually pass the mangaId here.
            // Koin will automatically provide the ViewModel with the correct SavedStateHandle.
            val viewModel: ReaderViewModel = koinViewModel()
            ReaderScreen(
                viewModel = viewModel,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
