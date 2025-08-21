package com.easycomic.ui_reader

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

/**
 * 阅读器主界面
 * 支持水平和垂直阅读模式，双指缩放，手势翻页
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings

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
            if (settings.isMenuVisible) {
                ReaderTopAppBar(
                    title = uiState.manga?.title ?: "",
                    currentPage = uiState.currentPage + 1,
                    maxPage = uiState.pageCount,
                    onBack = onBack,
                    viewModel = viewModel
                )
            }
        },
        bottomBar = {
            if (settings.isMenuVisible) {
                ReaderBottomBar(
                    currentPage = uiState.currentPage + 1,
                    maxPage = uiState.pageCount,
                    progress = uiState.readingProgress,
                    onPageSelected = { page -> viewModel.goToPage(page - 1) },
                    onPreviousPage = { viewModel.previousPage() },
                    onNextPage = { viewModel.nextPage() }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { viewModel.toggleMenu() })
                }
        ) {
            when {
                uiState.isLoading -> LoadingIndicator(progress = null)
                uiState.manga == null -> EmptyState()
                else -> {
                    when (settings.readingDirection) {
                        ReadingDirection.HORIZONTAL -> HorizontalReader(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                        ReadingDirection.VERTICAL -> VerticalReader(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HorizontalReader(
    uiState: ReaderUiState,
    viewModel: ReaderViewModel
) {
    val pagerState = rememberPagerState(initialPage = uiState.currentPage) {
        uiState.pageCount
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            viewModel.goToPage(pagerState.currentPage)
        }
    }
    
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .testTag("ReaderPager"),
        userScrollEnabled = !uiState.settings.isMenuVisible
    ) { pageIndex ->
        val pageBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = pageIndex) {
            try {
                val bitmap = viewModel.getPageBitmap(pageIndex)
                value = bitmap
            } catch (e: Exception) {
                value = null
            }
        }

        if (pageBitmap != null) {
            ComicPageDisplay(
                bitmap = pageBitmap!!,
                isLoading = uiState.isLoadingImage && pageIndex == uiState.currentPage,
                readingMode = uiState.settings.readingMode,
                onTap = { viewModel.toggleMenu() }
            )
        } else {
            LoadingIndicator(progress = null)
        }
    }
}

@Composable
private fun VerticalReader(uiState: ReaderUiState, viewModel: ReaderViewModel) {
    // Note: This is a performance-intensive implementation for vertical reading.
    // A production-ready app would need a more sophisticated solution, like paging.
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(uiState.pageCount) { pageIndex ->
            val pageBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = pageIndex) {
                try {
                    val bitmap = viewModel.getPageBitmap(pageIndex)
                    value = bitmap
                } catch (e: Exception) {
                    value = null
                }
            }

            if (pageBitmap != null) {
                Image(
                    bitmap = pageBitmap!!.asImageBitmap(),
                    contentDescription = "Page ${pageIndex + 1}",
                    contentScale = if (uiState.settings.readingMode == ReadingMode.FIT) ContentScale.Fit else ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Placeholder while loading
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp) // Placeholder height
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderTopAppBar(
    title: String,
    currentPage: Int,
    maxPage: Int,
    onBack: () -> Unit,
    viewModel: ReaderViewModel
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title.ifEmpty { "Reader" }, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (maxPage > 0) {
                Text(
                    text = "$currentPage/$maxPage",
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
            SettingsMenu(
                expanded = showMenu,
                onDismiss = { showMenu = false },
                viewModel = viewModel
            )
        }
    )
}


@Composable
fun SettingsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    viewModel: ReaderViewModel
) {
    val settings by viewModel.uiState.collectAsStateWithLifecycle()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // Reading Mode
        Text("Reading Mode", modifier = Modifier.padding(16.dp))
        DropdownMenuItem(
            text = { Text("Fit to Screen") },
            onClick = { viewModel.setReadingMode(ReadingMode.FIT) },
            leadingIcon = {
                RadioButton(
                    selected = settings.settings.readingMode == ReadingMode.FIT,
                    onClick = { viewModel.setReadingMode(ReadingMode.FIT) }
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Fill Screen") },
            onClick = { viewModel.setReadingMode(ReadingMode.FILL) },
            leadingIcon = {
                RadioButton(
                    selected = settings.settings.readingMode == ReadingMode.FILL,
                    onClick = { viewModel.setReadingMode(ReadingMode.FILL) }
                )
            }
        )
        HorizontalDivider()
        // Reading Direction
        Text("Reading Direction", modifier = Modifier.padding(16.dp))
        DropdownMenuItem(
            text = { Text("Horizontal") },
            onClick = { viewModel.setReadingDirection(ReadingDirection.HORIZONTAL) },
            leadingIcon = {
                RadioButton(
                    selected = settings.settings.readingDirection == ReadingDirection.HORIZONTAL,
                    onClick = { viewModel.setReadingDirection(ReadingDirection.HORIZONTAL) }
                )
            }
        )
        DropdownMenuItem(
            text = { Text("Vertical") },
            onClick = { viewModel.setReadingDirection(ReadingDirection.VERTICAL) },
            leadingIcon = {
                RadioButton(
                    selected = settings.settings.readingDirection == ReadingDirection.VERTICAL,
                    onClick = { viewModel.setReadingDirection(ReadingDirection.VERTICAL) }
                )
            }
        )
    }
}


@Composable
private fun ReaderBottomBar(
    currentPage: Int,
    maxPage: Int,
    progress: Float,
    onPageSelected: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
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
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(16.dp)
    ) {
        if (maxPage > 0) {
            Slider(
                value = currentPage.toFloat(),
                onValueChange = { onPageSelected(it.roundToInt()) },
                valueRange = 1f..maxPage.toFloat(),
                steps = if (maxPage > 1) maxPage - 2 else 0
            )
            
            // 显示阅读进度百分比
            Text(
                text = "进度: ${(progress * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousPage, enabled = currentPage > 1) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Page")
            }
            Text(
                text = "$currentPage/$maxPage",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable(enabled = maxPage > 0) { showPageSelector = true }
            )
            IconButton(onClick = onNextPage, enabled = currentPage < maxPage) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Page")
            }
        }
    }
}

@Composable
private fun ComicPageDisplay(
    bitmap: android.graphics.Bitmap,
    isLoading: Boolean,
    readingMode: ReadingMode,
    onTap: () -> Unit
) {
    var zoom by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastTapTime by remember { mutableStateOf(0L) }
    
    val contentScale = if (readingMode == ReadingMode.FIT) ContentScale.Fit else ContentScale.FillWidth
    
    // 缩放限制
    val minZoom = 0.5f
    val maxZoom = 5f
    val doubleTapZoom = 2f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    val newZoom = (zoom * gestureZoom).coerceIn(minZoom, maxZoom)
                    
                    // 计算新的偏移量，确保图片不会移出边界
                    val maxOffsetX = (size.width * (newZoom - 1)) / 2
                    val maxOffsetY = (size.height * (newZoom - 1)) / 2
                    
                    val newOffset = Offset(
                        x = (offset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                        y = (offset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                    )
                    
                    zoom = newZoom
                    offset = newOffset
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val currentTime = System.currentTimeMillis()
                        
                        // 检查是否为双击
                        if (currentTime - lastTapTime < 300) {
                            // 双击缩放
                            if (zoom > 1.5f) {
                                // 缩放到适合屏幕
                                zoom = 1f
                                offset = Offset.Zero
                            } else {
                                // 放大到双击缩放级别
                                zoom = doubleTapZoom
                                // 以点击位置为中心缩放
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                offset = Offset(
                                    x = (centerX - tapOffset.x) * (doubleTapZoom - 1),
                                    y = (centerY - tapOffset.y) * (doubleTapZoom - 1)
                                ).let { newOffset ->
                                    val maxOffsetX = (size.width * (doubleTapZoom - 1)) / 2
                                    val maxOffsetY = (size.height * (doubleTapZoom - 1)) / 2
                                    Offset(
                                        x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
                                        y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                }
                            }
                        } else {
                            // 单击切换菜单
                            onTap()
                        }
                        
                        lastTapTime = currentTime
                    }
                )
            }
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Comic Page",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoom,
                    scaleY = zoom,
                    translationX = offset.x,
                    translationY = offset.y
                ),
            contentScale = contentScale
        )
        
        // 显示缩放指示器
        if (zoom != 1f) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "${(zoom * 100).roundToInt()}%",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

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
        title = { Text("Go to Page") },
        text = {
            Column {
                Text("Page: ${selectedPage.roundToInt()} / $maxPage", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = selectedPage,
                    onValueChange = { selectedPage = it },
                    valueRange = 1f..maxPage.toFloat(),
                    steps = if (maxPage > 1) maxPage - 2 else 0
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onPageSelected(selectedPage.roundToInt()) }) {
                Text("Go")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ErrorDialog(message: String, onDismiss: () -> Unit, onBack: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onBack) {
                Text("Back")
            }
        },
        icon = { Icon(Icons.Default.Error, contentDescription = "Error") }
    )
}

@Composable
private fun LoadingIndicator(progress: Float?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading...")
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Error,
                contentDescription = "Empty",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No content to display",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}