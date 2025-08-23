package com.easycomic.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.easycomic.domain.model.ThemeMode
import com.easycomic.ui.animation.AppAnimations
import com.easycomic.ui.theme.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * 设置界面 - Phase 3 增强版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = koinViewModel()
) {
    val themePreference by themeViewModel.themePreference.collectAsState()
    var showAbout by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "设置",
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 主题设置区域
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = AppAnimations.slideInFromRight() + fadeIn(),
                    exit = AppAnimations.slideOutToRight() + fadeOut()
                ) {
                    SettingsSection(
                        title = "外观",
                        icon = Icons.Default.Palette,
                        description = "个性化您的阅读体验"
                    ) {
                        // 主题模式选择
                        ThemeModeSelector(
                            currentMode = themePreference.themeMode,
                            onModeSelected = themeViewModel::updateThemeMode
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 动态色彩开关
                        DynamicColorSwitch(
                            enabled = themePreference.useDynamicColors,
                            onToggle = { themeViewModel.toggleDynamicColors() }
                        )
                    }
                }
            }
            
            // 阅读设置区域
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = AppAnimations.slideInFromRight() + fadeIn(
                        animationSpec = tween(
                            durationMillis = AppAnimations.StandardDuration,
                            delayMillis = 100
                        )
                    ),
                    exit = AppAnimations.slideOutToRight() + fadeOut()
                ) {
                    SettingsSection(
                        title = "阅读",
                        icon = Icons.Default.Book,
                        description = "优化您的阅读设置"
                    ) {
                        // 阅读设置预留位置
                        Text(
                            text = "🚧 阅读设置功能正在开发中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "即将推出：翻页模式、缩放设置、全屏模式等",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 数据管理区域
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = AppAnimations.slideInFromRight() + fadeIn(
                        animationSpec = tween(
                            durationMillis = AppAnimations.StandardDuration,
                            delayMillis = 200
                        )
                    ),
                    exit = AppAnimations.slideOutToRight() + fadeOut()
                ) {
                    SettingsSection(
                        title = "数据管理",
                        icon = Icons.Default.Storage,
                        description = "管理您的漫画数据"
                    ) {
                        Text(
                            text = "🚧 数据管理功能正在开发中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "即将推出：数据备份、导入导出、清理缓存等",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 关于区域
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = AppAnimations.slideInFromRight() + fadeIn(
                        animationSpec = tween(
                            durationMillis = AppAnimations.StandardDuration,
                            delayMillis = 300
                        )
                    ),
                    exit = AppAnimations.slideOutToRight() + fadeOut()
                ) {
                    SettingsSection(
                        title = "关于",
                        icon = Icons.Default.Info,
                        description = "应用信息与版本"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Easy Comic",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "版本 1.0.0-alpha (Phase 3)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = { showAbout = true }
                            ) {
                                Text("详情")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 关于对话框
    if (showAbout) {
        AboutDialog(
            onDismiss = { showAbout = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = if (description != null) 8.dp else 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun ThemeModeSelector(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.selectableGroup()) {
        Text(
            text = "主题模式",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        ThemeModeOption(
            text = "跟随系统",
            icon = Icons.Default.Brightness6,
            description = "根据系统设置自动切换",
            selected = currentMode == ThemeMode.SYSTEM,
            onClick = { onModeSelected(ThemeMode.SYSTEM) }
        )
        
        ThemeModeOption(
            text = "亮色模式",
            icon = Icons.Default.LightMode,
            description = "始终使用亮色主题",
            selected = currentMode == ThemeMode.LIGHT,
            onClick = { onModeSelected(ThemeMode.LIGHT) }
        )
        
        ThemeModeOption(
            text = "暗色模式",
            icon = Icons.Default.DarkMode,
            description = "始终使用暗色主题",
            selected = currentMode == ThemeMode.DARK,
            onClick = { onModeSelected(ThemeMode.DARK) }
        )
    }
}

@Composable
private fun ThemeModeOption(
    text: String,
    icon: ImageVector,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        animationSpec = AppAnimations.themeTransitionSpec(),
        label = "ThemeOptionAlpha"
    )
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = animatedAlpha)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = animatedAlpha)
                )
            }
        }
    }
}

@Composable
private fun DynamicColorSwitch(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "动态色彩",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "使用 Material You 动态色彩（需要 Android 12+）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = enabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Easy Comic",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "专业的 Android 漫画阅读器\n" +
                          "版本: 1.0.0-alpha (Phase 3)\n" +
                          "构建时间: 2025年8月21日",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "✨ Phase 3 新功能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Material Design 3 完整适配\n" +
                          "• 动态主题系统\n" +
                          "• 流畅的页面过渡动画\n" +
                          "• 增强的设置界面",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}
