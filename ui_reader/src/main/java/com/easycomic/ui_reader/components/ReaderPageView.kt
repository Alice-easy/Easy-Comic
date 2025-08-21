package com.easycomic.ui_reader.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

/**
 * 阅读器页面视图组件
 * 整合了缩放图片和手势控制功能
 */
@Composable
fun ReaderPageView(
    imageUrl: String,
    currentPage: Int,
    totalPages: Int,
    isControlsVisible: Boolean,
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onToggleControls: () -> Unit = {},
    onBookmark: () -> Unit = {},
    onSettings: () -> Unit = {},
    onBack: () -> Unit = {},
    gestureConfig: GestureConfig = rememberGestureConfig()
) {
    var zoomState by rememberZoomState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 主要内容区域 - 可缩放图片
        ReaderGestureHandler(
            modifier = Modifier.fillMaxSize(),
            onPreviousPage = onPreviousPage,
            onNextPage = onNextPage,
            onToggleControls = onToggleControls,
            onDoubleTap = { offset ->
                // 双击缩放逻辑已在ZoomableImage中处理
            },
            enableSwipeNavigation = gestureConfig.enableSwipeNavigation && !zoomState.isZoomed,
            enableTapNavigation = gestureConfig.enableTapNavigation && !zoomState.isZoomed
        ) {
            ZoomableImage(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize(),
                contentDescription = "漫画页面 $currentPage",
                onDoubleTap = { offset ->
                    // 可以在这里添加双击反馈
                }
            )
        }
        
        // 顶部控制栏
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .zIndex(1f)
        ) {
            TopControlBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onBack = onBack,
                onBookmark = onBookmark,
                onSettings = onSettings
            )
        }
        
        // 底部控制栏
        AnimatedVisibility(
            visible = isControlsVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(1f)
        ) {
            BottomControlBar(
                currentPage = currentPage,
                totalPages = totalPages,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                onPageSeek = { page ->
                    // 处理页面跳转
                }
            )
        }
    }
}

/**
 * 顶部控制栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopControlBar(
    currentPage: Int,
    totalPages: Int,
    onBack: () -> Unit,
    onBookmark: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = "$currentPage / $totalPages",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            IconButton(onClick = onBookmark) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "书签",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    )
}

/**
 * 底部控制栏
 */
@Composable
private fun BottomControlBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onPageSeek: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 进度条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentPage.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(40.dp)
                )
                
                Slider(
                    value = currentPage.toFloat(),
                    onValueChange = { value ->
                        onPageSeek(value.toInt())
                    },
                    valueRange = 1f..totalPages.toFloat(),
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Text(
                    text = totalPages.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 导航按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onPreviousPage,
                    enabled = currentPage > 1
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateBefore,
                        contentDescription = "上一页",
                        tint = if (currentPage > 1) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.outline
                    )
                }
                
                IconButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages
                ) {
                    Icon(
                        imageVector = Icons.Default.NavigateNext,
                        contentDescription = "下一页",
                        tint = if (currentPage < totalPages) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}