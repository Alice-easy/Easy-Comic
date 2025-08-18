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
            value = viewModel.getPageBitmap(pageIndex)
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
                value = viewModel.getPageBitmap(pageIndex)
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        Divider()
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
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousPage, enabled = currentPage > 1) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Page")
            }
            Text(
                text = "$currentPage/$maxPage",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable(enabled = maxPage > 0) { showPageSelector = true }
            )
            IconButton(onClick = onNextPage, enabled = currentPage < maxPage) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Page")
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
    val contentScale = if (readingMode == ReadingMode.FIT) ContentScale.Fit else ContentScale.FillWidth

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, gestureZoom, _ ->
                    zoom = (zoom * gestureZoom).coerceIn(0.5f, 5f)
                    offset += pan
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onTap() })
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