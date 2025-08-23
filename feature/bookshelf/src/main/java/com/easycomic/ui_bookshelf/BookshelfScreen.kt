package com.easycomic.ui_bookshelf

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.easycomic.data.repository.ImportProgress
import com.easycomic.domain.model.Manga
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel = koinViewModel(),
    onNavigateToReader: (Long) -> Unit,
    onNavigateToSettings: () -> Unit = {}
) {
    val mangas by viewModel.getComics().collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val importProgress by viewModel.importProgress.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedMangas by viewModel.selectedMangas.collectAsState()
    
    var showSearchBar by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showBatchMenu by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                // 使用新的SAF导入功能
                viewModel.importFromDocumentTree(uri)
            }
        }
    )
    
    // 显示导入进度对话框
    importProgress?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = { 
                if (progress is ImportProgress.Completed || progress is ImportProgress.Error) {
                    viewModel.clearImportProgress()
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChanged = viewModel::searchComics,
                    onSearchDismissed = {
                        showSearchBar = false
                        viewModel.searchComics("")
                    }
                )
            } else if (selectionMode) {
                SelectionTopAppBar(
                    selectedCount = selectedMangas.size,
                    onClearSelection = { viewModel.clearSelection() },
                    onSelectAll = { viewModel.selectAllVisibleMangas(mangas) },
                    onBatchAction = { showBatchMenu = true }
                )
            } else {
                TopAppBar(
                    title = { Text("书架") },
                    actions = {
                        IconButton(
                            onClick = { viewModel.refreshComics() },
                            enabled = !isLoading && !isImporting
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "刷新")
                        }
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "排序")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            currentSortOrder = sortOrder,
                            onSortOrderSelected = { order ->
                                viewModel.setSortOrder(order)
                                showSortMenu = false
                            },
                            onDismiss = { showSortMenu = false }
                        )
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { openDocumentLauncher.launch(null) },
                modifier = Modifier.padding(16.dp)
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = "导入漫画")
                }
            }
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在加载漫画...")
                    }
                }
            }
            mangas.isEmpty() -> {
                EmptyState(
                    message = if (searchQuery.isNotBlank()) "未找到匹配的漫画" else "还没有漫画，点击右下角按钮导入"
                )
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(128.dp),
                    modifier = Modifier
                        .padding(padding)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mangas, key = { it.id }) { manga ->
                        GridComicCard(
                            manga = manga,
                            searchQuery = searchQuery,
                            isSelected = selectedMangas.contains(manga.id),
                            selectionMode = selectionMode,
                            onClick = { 
                                if (selectionMode) {
                                    viewModel.toggleMangaSelection(manga.id)
                                } else {
                                    onNavigateToReader(manga.id)
                                }
                            },
                            onLongClick = { 
                                if (!selectionMode) {
                                    viewModel.enterSelectionMode(manga.id)
                                } else {
                                    viewModel.toggleMangaSelection(manga.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // 批量操作菜单
    if (showBatchMenu) {
        BatchActionBottomSheet(
            selectedCount = selectedMangas.size,
            onDeleteSelected = {
                viewModel.deleteSelectedMangas()
                showBatchMenu = false
            },
            onMarkAsFavorite = {
                viewModel.markSelectedAsFavorite(true)
                showBatchMenu = false
            },
            onMarkAsNotFavorite = {
                viewModel.markSelectedAsFavorite(false)
                showBatchMenu = false
            },
            onMarkAsRead = {
                viewModel.markSelectedAsRead()
                showBatchMenu = false
            },
            onDismiss = { showBatchMenu = false }
        )
    }
}

@Composable
private fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { 
            if (progress is ImportProgress.Completed || progress is ImportProgress.Error) {
                onDismiss()
            }
        },
        title = { 
            Text(
                text = when (progress) {
                    is ImportProgress.Scanning -> "正在扫描文件..."
                    is ImportProgress.Found -> "找到漫画文件"
                    is ImportProgress.Processing -> "正在导入..."
                    is ImportProgress.Completed -> "导入完成"
                    is ImportProgress.Error -> "导入失败"
                }
            )
        },
        text = {
            Column {
                when (progress) {
                    is ImportProgress.Scanning -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Text("正在扫描目录中的漫画文件...")
                        }
                    }
                    is ImportProgress.Found -> {
                        Text("找到 ${progress.count} 个漫画文件，准备导入...")
                    }
                    is ImportProgress.Processing -> {
                        Column {
                            Text("正在导入: ${progress.processed} / ${progress.total}")
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = progress.processed.toFloat() / progress.total.toFloat(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is ImportProgress.Completed -> {
                        Text("成功导入 ${progress.imported} 个漫画文件")
                    }
                    is ImportProgress.Error -> {
                        Text(
                            text = progress.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (progress is ImportProgress.Completed || progress is ImportProgress.Error) {
                TextButton(onClick = onDismiss) {
                    Text("确定")
                }
            }
        }
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MangaItem(manga: Manga, onMangaClick: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onMangaClick(manga.id) }
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            // 漫画封面加载
            ComicCard(
                manga = manga,
                onClick = { onMangaClick(manga.id) },
                onLongClick = { /* TODO: 实现长按选择 */ },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchDismissed: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("搜索漫画...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onSearchDismissed) {
                Icon(Icons.Default.ArrowBack, contentDescription = "取消搜索")
            }
        }
    )
}

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSortOrder: BookshelfViewModel.SortOrder,
    onSortOrderSelected: (BookshelfViewModel.SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text("标题 A-Z") },
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.TITLE_ASC) },
            trailingIcon = if (currentSortOrder == BookshelfViewModel.SortOrder.TITLE_ASC) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        DropdownMenuItem(
            text = { Text("标题 Z-A") },
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.TITLE_DESC) },
            trailingIcon = if (currentSortOrder == BookshelfViewModel.SortOrder.TITLE_DESC) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        DropdownMenuItem(
            text = { Text("添加时间 (最新)") },
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.DATE_ADDED_DESC) },
            trailingIcon = if (currentSortOrder == BookshelfViewModel.SortOrder.DATE_ADDED_DESC) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        DropdownMenuItem(
            text = { Text("最近阅读") },
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.LAST_READ_DESC) },
            trailingIcon = if (currentSortOrder == BookshelfViewModel.SortOrder.LAST_READ_DESC) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        DropdownMenuItem(
            text = { Text("阅读进度") },
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.PROGRESS_DESC) },
            trailingIcon = if (currentSortOrder == BookshelfViewModel.SortOrder.PROGRESS_DESC) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onBatchAction: () -> Unit
) {
    TopAppBar(
        title = { Text("已选择 $selectedCount 项") },
        navigationIcon = {
            IconButton(onClick = onClearSelection) {
                Icon(Icons.Default.Close, contentDescription = "取消选择")
            }
        },
        actions = {
            IconButton(onClick = onSelectAll) {
                Icon(Icons.Default.CheckBox, contentDescription = "全选")
            }
            IconButton(onClick = onBatchAction) {
                Icon(Icons.Default.MoreVert, contentDescription = "批量操作")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchActionBottomSheet(
    selectedCount: Int,
    onDeleteSelected: () -> Unit,
    onMarkAsFavorite: () -> Unit,
    onMarkAsNotFavorite: () -> Unit,
    onMarkAsRead: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "批量操作 ($selectedCount 项)",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ListItem(
                headlineContent = { Text("添加到收藏") },
                leadingContent = {
                    Icon(Icons.Default.Favorite, contentDescription = null)
                },
                modifier = Modifier.clickable { onMarkAsFavorite() }
            )
            
            ListItem(
                headlineContent = { Text("从收藏移除") },
                leadingContent = {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null)
                },
                modifier = Modifier.clickable { onMarkAsNotFavorite() }
            )
            
            ListItem(
                headlineContent = { Text("标记为已读") },
                leadingContent = {
                    Icon(Icons.Default.Done, contentDescription = null)
                },
                modifier = Modifier.clickable { onMarkAsRead() }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            ListItem(
                headlineContent = { Text("删除选中项") },
                leadingContent = {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                modifier = Modifier.clickable { showDeleteConfirmation = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 $selectedCount 个漫画吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSelected()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("取消")
                }
            }
        )
    }
}