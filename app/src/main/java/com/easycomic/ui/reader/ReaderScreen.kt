package com.easycomic.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import timber.log.Timber
import kotlin.math.roundToInt

/**
 * 简化的阅读器界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    filePath: String,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    
    // 设置屏幕尺寸
    LaunchedEffect(Unit) {
        viewModel.setScreenSize(
            width = configuration.screenWidthDp,
            height = configuration.screenHeightDp
        )
    }
    
    // 加载漫画
    LaunchedEffect(filePath) {
        viewModel.loadComic(filePath)
    }
    
    // 显示错误对话框
    val errorMessage = uiState.error
    if (errorMessage != null) {
        ErrorDialog(
            message = errorMessage,
            onDismiss = { viewModel.clearError() },
            onBack = onBack
        )
    }
    
    Scaffold(
        topBar = {
            ReaderTopAppBar(
                title = uiState.comicTitle,
                currentPage = uiState.currentPage + 1,
                maxPage = uiState.maxPage + 1,
                onBack = onBack,
                onInfo = { /* 显示信息对话框 */ }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentPage = uiState.currentPage + 1,
                maxPage = uiState.maxPage + 1,
                progress = viewModel.getCurrentProgress(),
                onPageSelected = { page -> viewModel.goToPage(page - 1) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator(progress = uiState.progress)
                }
                uiState.currentPageBitmap != null -> {
                    ComicPageDisplay(
                        bitmap = uiState.currentPageBitmap!!,
                        isLoading = uiState.isLoadingImage,
                        onPagePrevious = { viewModel.previousPage() },
                        onPageNext = { viewModel.nextPage() }
                    )
                }
                uiState.isLoadingImage -> {
                    LoadingIndicator(progress = null)
                }
                else -> {
                    EmptyState()
                }
            }
        }
    }
}

/**
 * 顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopAppBar(
    title: String,
    currentPage: Int,
    maxPage: Int,
    onBack: () -> Unit,
    onInfo: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title.ifEmpty { "漫画阅读器" }) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            Text(
                text = "$currentPage/$maxPage",
                modifier = Modifier.padding(end = 16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(onClick = onInfo) {
                Icon(Icons.Default.Info, contentDescription = "信息")
            }
        }
    )
}

/**
 * 底部控制栏
 */
@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    maxPage: Int,
    progress: Float,
    onPageSelected: (Int) -> Unit
) {
    var showPageSelector by remember { mutableStateOf(false) }
    
    if (showPageSelector) {
        PageSelectorDialog(
            currentPage = currentPage,
            maxPage = maxPage,
            onDismiss = { showPageSelector = false },
            onPageSelected = { page ->
                showPageSelector = false
                onPageSelected(page)
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // 进度条
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一页按钮
            IconButton(
                onClick = { onPageSelected(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "上一页")
            }
            
            // 页码信息
            Text(
                text = "$currentPage/$maxPage",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { showPageSelector = true }
            )
            
            // 下一页按钮
            IconButton(
                onClick = { onPageSelected(currentPage + 1) },
                enabled = currentPage < maxPage
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "下一页")
            }
        }
    }
}

/**
 * 漫画页面显示
 */
@Composable
private fun ComicPageDisplay(
    bitmap: android.graphics.Bitmap,
    isLoading: Boolean,
    onPagePrevious: () -> Unit,
    onPageNext: () -> Unit
) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
            .clickable { /* 点击可以显示/隐藏控制栏 */ }
    ) {
        // 图像显示
        AsyncImage(
            model = bitmap.asImageBitmap(),
            contentDescription = "漫画页面",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = ContentScale.Fit
        )
        
        // 加载指示器
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Center)
            )
        }
        
        // 左右点击区域（翻页）
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // 左侧点击区域（上一页）
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f)
                    .align(Alignment.CenterStart)
                    .clickable { onPagePrevious() }
            )
            
            // 右侧点击区域（下一页）
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f)
                    .align(Alignment.CenterEnd)
                    .clickable { onPageNext() }
            )
        }
    }
}

/**
 * 页面选择对话框
 */
@Composable
private fun PageSelectorDialog(
    currentPage: Int,
    maxPage: Int,
    onDismiss: () -> Unit,
    onPageSelected: (Int) -> Unit
) {
    var selectedPage by remember { mutableStateOf(currentPage.toFloat()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择页面") },
        text = {
            Column {
                Text("当前页面: $currentPage / $maxPage")
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = selectedPage,
                    onValueChange = { selectedPage = it },
                    valueRange = 1f..maxPage.toFloat(),
                    steps = maxPage - 1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "页面: ${selectedPage.roundToInt()}",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onPageSelected(selectedPage.roundToInt())
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 错误对话框
 */
@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    onBack: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("错误") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onBack) {
                Text("返回")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("忽略")
            }
        },
        icon = {
            Icon(Icons.Default.Error, contentDescription = "错误")
        }
    )
}

/**
 * 加载指示器
 */
@Composable
private fun LoadingIndicator(progress: Float?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (progress != null && progress > 0) {
                CircularProgressIndicator(progress = progress)
                Spacer(modifier = Modifier.height(16.dp))
                Text("加载中... ${(progress * 100).toInt()}%")
            } else {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("加载中...")
            }
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = "无内容",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "无内容显示",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}