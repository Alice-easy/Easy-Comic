package com.easycomic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import com.easycomic.ui_bookshelf.BookshelfScreen
import com.easycomic.ui_reader.ReaderScreen
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                EasyComicApp()
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EasyComicApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                listOf(
                    BottomNavigationItem.Bookshelf,
                    BottomNavigationItem.Settings
                ).forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavigationItem.Bookshelf.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavigationItem.Bookshelf.route) {
                BookshelfScreen(
                    onNavigateToSettings = {
                        navController.navigate(BottomNavigationItem.Settings.route)
                    },
                    onNavigateToReader = { mangaId ->
                        navController.navigate("reader/$mangaId")
                    }
                )
            }
            
            composable(BottomNavigationItem.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable("reader/{mangaId}") { backStackEntry ->
                val mangaId = backStackEntry.arguments?.getString("mangaId")?.toLongOrNull() ?: 0L
                val readerViewModel: com.easycomic.ui_reader.ReaderViewModel = koinViewModel()
                LaunchedEffect(mangaId) {
                    if (mangaId > 0) {
                        readerViewModel.setMangaId(mangaId)
                    }
                }
                ReaderScreen(
                    viewModel = readerViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class BottomNavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Bookshelf : BottomNavigationItem(
        route = "bookshelf",
        title = "书架",
        icon = Icons.Filled.Book
    )
    
    object Settings : BottomNavigationItem(
        route = "settings",
        title = "设置",
        icon = Icons.Filled.Settings
    )
}

@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Easy Comic",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "现代化 Android 漫画阅读器",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "📱 Easy Comic 漫画阅读器",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• 书架管理功能 ✅\n• 阅读器功能 ✅\n• Clean Architecture ✅\n• ZIP/RAR 支持 ✅",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Version 0.6.0-alpha",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onNavigateBack) {
            Text("返回书架")
        }
    }
}