package com.easycomic.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
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
                title = uiState.manga?.title ?: "",
                currentPage = uiState.currentPage + 1,
                maxPage = uiState.pageCount,
                onBack = onBack,
                onInfo = { /* TODO: Show manga info dialog */ }
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentPage = uiState.currentPage + 1,
                maxPage = uiState.pageCount,
                onPageSelected = { page -> viewModel.goToPage(page - 1) },
                onPreviousPage = { viewModel.previousPage() },
                onNextPage = { viewModel.nextPage() }
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
                    LoadingIndicator(progress = null)
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
            IconButton(onClick = onInfo) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
        }
    )
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
            .background(MaterialTheme.colorScheme.surface)
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
                    zoom = (zoom * gestureZoom).coerceIn(0.5f, 5f)
                    offset += pan
                }
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
            contentScale = ContentScale.Fit
        )

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        Row(Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxHeight().weight(0.3f).clickable(onClick = onPagePrevious))
            Spacer(modifier = Modifier.fillMaxHeight().weight(0.4f))
            Box(modifier = Modifier.fillMaxHeight().weight(0.3f).clickable(onClick = onPageNext))
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