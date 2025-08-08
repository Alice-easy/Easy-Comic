package com.easycomic.presentation.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.easycomic.presentation.ui.NavRoutes


@Composable
fun EasyComicNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = NavRoutes.Bookshelf) {
        composable(NavRoutes.Bookshelf) { Text("Bookshelf") }
        composable(NavRoutes.Favorites) { Text("Favorites") }
        composable(NavRoutes.Settings) { Text("Settings") }
    }
}
