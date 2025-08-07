package com.easycomic.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.easycomic.R
import com.easycomic.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getStatistics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Statistics section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Library Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatItem(
                                title = "Total",
                                value = uiState.totalMangaCount.toString(),
                                icon = Icons.Default.Book
                            )
                            StatItem(
                                title = "Favorites",
                                value = uiState.favoriteMangaCount.toString(),
                                icon = Icons.Default.Favorite
                            )
                            StatItem(
                                title = "Completed",
                                value = uiState.completedMangaCount.toString(),
                                icon = Icons.Default.DoneAll
                            )
                        }
                    }
                }
            }
            
            item {
                // Theme settings
                SettingsSection(title = "Appearance") {
                    ThemeSetting(
                        currentTheme = uiState.theme,
                        onThemeChanged = { viewModel.updateTheme(it) }
                    )
                }
            }
            
            item {
                // Reader settings
                SettingsSection(title = stringResource(R.string.settings_reader)) {
                    ReaderDirectionSetting(
                        currentDirection = uiState.readerDirection,
                        onDirectionChanged = { viewModel.updateReaderDirection(it) }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DefaultZoomSetting(
                        currentZoomMode = uiState.defaultZoomMode,
                        onZoomModeChanged = { viewModel.updateDefaultZoomMode(it) }
                    )
                }
            }
            
            item {
                // WebDAV settings
                SettingsSection(title = stringResource(R.string.settings_webdav)) {
                    WebDavSettings(
                        uiState = uiState,
                        onSettingsChanged = { url, username, password, enabled ->
                            viewModel.updateWebDavSettings(url, username, password, enabled)
                        },
                        onTestConnection = { url, username, password ->
                            viewModel.testWebDavConnection(url, username, password)
                        },
                        onSync = { viewModel.syncWithWebDAV() }
                    )
                }
            }
            
            item {
                // About section
                SettingsSection(title = "About") {
                    AboutItem()
                }
            }
        }
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ThemeSetting(
    currentTheme: SettingsViewModel.AppTheme,
    onThemeChanged: (SettingsViewModel.AppTheme) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsViewModel.AppTheme.values().forEach { theme ->
                FilterChip(
                    selected = currentTheme == theme,
                    onClick = { onThemeChanged(theme) },
                    label = {
                        Text(
                            text = when (theme) {
                                SettingsViewModel.AppTheme.SYSTEM -> stringResource(R.string.settings_theme_system)
                                SettingsViewModel.AppTheme.LIGHT -> stringResource(R.string.settings_theme_light)
                                SettingsViewModel.AppTheme.DARK -> stringResource(R.string.settings_theme_dark)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ReaderDirectionSetting(
    currentDirection: SettingsViewModel.ReaderDirection,
    onDirectionChanged: (SettingsViewModel.ReaderDirection) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_reader_direction),
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsViewModel.ReaderDirection.values().forEach { direction ->
                FilterChip(
                    selected = currentDirection == direction,
                    onClick = { onDirectionChanged(direction) },
                    label = {
                        Text(
                            text = when (direction) {
                                SettingsViewModel.ReaderDirection.LEFT_TO_RIGHT -> stringResource(R.string.settings_reader_direction_left_to_right)
                                SettingsViewModel.ReaderDirection.RIGHT_TO_LEFT -> stringResource(R.string.settings_reader_direction_right_to_left)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DefaultZoomSetting(
    currentZoomMode: SettingsViewModel.ZoomMode,
    onZoomModeChanged: (SettingsViewModel.ZoomMode) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_reader_zoom),
            style = MaterialTheme.typography.titleSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsViewModel.ZoomMode.values().forEach { zoomMode ->
                FilterChip(
                    selected = currentZoomMode == zoomMode,
                    onClick = { onZoomModeChanged(zoomMode) },
                    label = {
                        Text(
                            text = when (zoomMode) {
                                SettingsViewModel.ZoomMode.FIT_TO_SCREEN -> stringResource(R.string.settings_reader_zoom_fit)
                                SettingsViewModel.ZoomMode.FILL_SCREEN -> stringResource(R.string.settings_reader_zoom_fill)
                                SettingsViewModel.ZoomMode.ORIGINAL_SIZE -> stringResource(R.string.settings_reader_zoom_original)
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun WebDavSettings(
    uiState: SettingsViewModel.SettingsUiState,
    onSettingsChanged: (String, String, String, Boolean) -> Unit,
    onTestConnection: (String, String, String) -> Unit,
    onSync: () -> Unit
) {
    var url by remember { mutableStateOf(uiState.webDavUrl) }
    var username by remember { mutableStateOf(uiState.webDavUsername) }
    var password by remember { mutableStateOf(uiState.webDavPassword) }
    var enabled by remember { mutableStateOf(uiState.webDavEnabled) }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // Enable switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.settings_webdav_enabled),
                style = MaterialTheme.typography.titleSmall
            )
            Switch(
                checked = enabled,
                onCheckedChange = { 
                    enabled = it
                    onSettingsChanged(url, username, password, it)
                }
            )
        }
        
        if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // URL field
            OutlinedTextField(
                value = url,
                onValueChange = { 
                    url = it
                    onSettingsChanged(it, username, password, enabled)
                },
                label = { Text(stringResource(R.string.settings_webdav_url)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it
                    onSettingsChanged(url, it, password, enabled)
                },
                label = { Text(stringResource(R.string.settings_webdav_username)) },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    onSettingsChanged(url, username, it, enabled)
                },
                label = { Text(stringResource(R.string.settings_webdav_password)) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Connection test button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onTestConnection(url, username, password) },
                    modifier = Modifier.weight(1f),
                    enabled = url.isNotBlank() && username.isNotBlank()
                ) {
                    if (uiState.isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.settings_webdav_test))
                    }
                }
                
                Button(
                    onClick = onSync,
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isSyncing && uiState.connectionTestResult == true
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sync")
                    }
                }
            }
            
            // Connection test result
            uiState.connectionTestResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (result) stringResource(R.string.settings_webdav_success) else stringResource(R.string.settings_webdav_failed),
                    color = if (result) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Sync result
            uiState.syncResult?.let { result ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (result) "Sync successful" else "Sync failed",
                    color = if (result) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun AboutItem() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open about dialog */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "Easy Comic v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}