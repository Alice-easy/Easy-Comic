package com.easycomic.presentation.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.easycomic.R

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    object Bookshelf : BottomNavItem(
        route = "bookshelf",
        titleResId = R.string.nav_bookshelf,
        icon = Icons.Default.Book
    )
    
    object Favorites : BottomNavItem(
        route = "favorites",
        titleResId = R.string.nav_favorites,
        icon = Icons.Default.Favorite
    )
    
    object Settings : BottomNavItem(
        route = "settings",
        titleResId = R.string.nav_settings,
        icon = Icons.Default.Settings
    )
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem> = listOf(
        BottomNavItem.Bookshelf,
        BottomNavItem.Favorites,
        BottomNavItem.Settings
    )
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                label = { Text(stringResource(item.titleResId)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}