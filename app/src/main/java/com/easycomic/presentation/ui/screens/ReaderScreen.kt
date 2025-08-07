package com.easycomic.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.easycomic.R
import com.easycomic.presentation.viewmodel.ReaderViewModel
import kotlinx.coroutines.delay

@Composable
fun ReaderScreen(
    mangaId: Long,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(mangaId) {
        viewModel.loadManga(mangaId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.manga?.title ?: "Reader",
                        maxLines = 1
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBookmarkDialog() }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks")
                    }
                    IconButton(onClick = { /* Open settings */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Reader settings")
                    }
                }
            )
        },
        bottomBar = {
            ReaderControls(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPrevious = { viewModel.onPreviousPage() },
                onNext = { viewModel.onNextPage() },
                onPageChanged = { viewModel.goToPage(it) },
                isRtl = uiState.readerDirection == ReaderViewModel.ReaderDirection.RIGHT_TO_LEFT
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Error loading page",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.clearError() }) {
                                Text(stringResource(R.string.common_retry))
                            }
                        }
                    }
                }
                
                else -> {
                    ReaderPage(
                        imagePath = "page_${uiState.currentPage}.jpg", // This should come from the manga file
                        zoomMode = uiState.zoomMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Page number indicator (auto-hide)
            var showPageNumber by remember { mutableStateOf(true) }
            LaunchedEffect(uiState.currentPage) {
                showPageNumber = true
                delay(2000)
                showPageNumber = false
            }
            
            if (showPageNumber) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reader_page_number, uiState.currentPage + 1, uiState.totalPages),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Bookmark dialog
        if (uiState.isBookmarkDialogOpen) {
            BookmarkDialog(
                onDismiss = { viewModel.closeBookmarkDialog() },
                onAddBookmark = { name, note -> viewModel.addBookmark(name, note) }
            )
        }
    }
}

@Composable
fun ReaderPage(
    imagePath: String,
    zoomMode: ReaderViewModel.ZoomMode,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
            .background(MaterialTheme.colorScheme.background)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = "Manga page",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentScale = when (zoomMode) {
                ReaderViewModel.ZoomMode.FIT_TO_SCREEN -> ContentScale.Fit
                ReaderViewModel.ZoomMode.FILL_SCREEN -> ContentScale.Crop
                ReaderViewModel.ZoomMode.ORIGINAL_SIZE -> ContentScale.None
            }
        )
    }
}

@Composable
fun ReaderControls(
    currentPage: Int,
    totalPages: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onPageChanged: (Int) -> Unit,
    isRtl: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = if (isRtl) onNext else onPrevious,
                enabled = if (isRtl) currentPage < totalPages - 1 else currentPage > 0
            ) {
                Icon(
                    imageVector = if (isRtl) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                    contentDescription = if (isRtl) "Next page" else "Previous page"
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                LinearProgressIndicator(
                    progress = if (totalPages > 0) (currentPage + 1f) / totalPages else 0f,
                    modifier = Modifier
                        .width(100.dp)
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            IconButton(
                onClick = if (isRtl) onPrevious else onNext,
                enabled = if (isRtl) currentPage > 0 else currentPage < totalPages - 1
            ) {
                Icon(
                    imageVector = if (isRtl) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
                    contentDescription = if (isRtl) "Previous page" else "Next page"
                )
            }
        }
    }
}

@Composable
fun BookmarkDialog(
    onDismiss: () -> Unit,
    onAddBookmark: (String, String?) -> Unit
) {
    var bookmarkName by remember { mutableStateOf("") }
    var bookmarkNote by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bookmark") },
        text = {
            Column {
                OutlinedTextField(
                    value = bookmarkName,
                    onValueChange = { bookmarkName = it },
                    label = { Text("Bookmark name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = bookmarkNote,
                    onValueChange = { bookmarkNote = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddBookmark(bookmarkName, bookmarkNote.ifBlank { null })
                },
                enabled = bookmarkName.isNotBlank()
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}