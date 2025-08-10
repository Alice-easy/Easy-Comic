package com.easycomic.ui.bookshelf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.easycomic.domain.model.Manga
import com.easycomic.ui.bookshelf.BookshelfViewModel.SortOption
import com.easycomic.ui.bookshelf.BookshelfViewModel.FilterOption

/**
 * 书架界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(
    viewModel: BookshelfViewModel,
    navController: NavController,
    onMangaClick: (Manga) -> Unit = {},
    onAddMangaClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val filterOption by viewModel.filterOption.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedMangaIds by viewModel.selectedMangaIds.collectAsStateWithLifecycle()
    
    var showSearch by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            BookshelfTopAppBar(
                title = if (selectionMode) {
                    "已选择 ${selectedMangaIds.size} 项"
                } else {
                    "书架"
                },
                showSearch = showSearch,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.searchManga(it) },
                onSearchToggle = { showSearch = !showSearch },
                onSortClick = { showSortDialog = true },
                onFilterClick = { showFilterDialog = true },
                onAddClick = onAddMangaClick,
                selectionMode = selectionMode,
                onSelectAll = { viewModel.selectAll() },
                onDeselectAll = { viewModel.deselectAll() },
                onDeleteSelected = { viewModel.deleteSelectedManga() },
                onCloseSelection = { viewModel.toggleSelectionMode() }
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
                    LoadingIndicator()
                }
                
                uiState.error != null -> {
                    ErrorScreen(
                        error = uiState.error!!,
                        onRetry = { viewModel.clearError() }
                    )
                }
                
                uiState.filteredMangaList.isEmpty() -> {
                    EmptyScreen(
                        searchQuery = searchQuery,
                        onAddMangaClick = onAddMangaClick
                    )
                }
                
                else -> {
                    MangaGrid(
                        mangaList = uiState.filteredMangaList,
                        selectionMode = selectionMode,
                        selectedMangaIds = selectedMangaIds,
                        onMangaClick = { manga ->
                            if (selectionMode) {
                                viewModel.toggleMangaSelection(manga.id)
                            } else {
                                onMangaClick(manga)
                            }
                        },
                        onMangaLongClick = { manga ->
                            viewModel.toggleMangaSelection(manga.id)
                            if (!selectionMode) {
                                viewModel.toggleSelectionMode()
                            }
                        },
                        onFavoriteClick = { manga ->
                            viewModel.toggleFavorite(manga.id)
                        }
                    )
                }
            }
        }
        
        // 排序对话框
        if (showSortDialog) {
            SortDialog(
                currentSort = sortOption,
                onSortSelected = { sort ->
                    viewModel.setSortOption(sort)
                    showSortDialog = false
                },
                onDismiss = { showSortDialog = false }
            )
        }
        
        // 筛选对话框
        if (showFilterDialog) {
            FilterDialog(
                currentFilter = filterOption,
                onFilterSelected = { filter ->
                    viewModel.setFilterOption(filter)
                    showFilterDialog = false
                },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

/**
 * 书架顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookshelfTopAppBar(
    title: String,
    showSearch: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onAddClick: () -> Unit,
    selectionMode: Boolean,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCloseSelection: () -> Unit
) {
    TopAppBar(
        title = {
            if (showSearch) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("搜索漫画...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
            } else {
                Text(text = title)
            }
        },
        navigationIcon = {
            if (selectionMode) {
                IconButton(onClick = onCloseSelection) {
                    Icon(Icons.Default.Close, contentDescription = "关闭选择")
                }
            } else {
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (showSearch) "关闭搜索" else "搜索"
                    )
                }
            }
        },
        actions = {
            if (!showSearch) {
                if (selectionMode) {
                    IconButton(onClick = onSelectAll) {
                        Icon(Icons.Default.SelectAll, contentDescription = "全选")
                    }
                    IconButton(onClick = onDeselectAll) {
                        Icon(Icons.Default.Deselect, contentDescription = "取消全选")
                    }
                    IconButton(onClick = onDeleteSelected) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                } else {
                    IconButton(onClick = onSortClick) {
                        Icon(Icons.Default.Sort, contentDescription = "排序")
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = "筛选")
                    }
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "添加漫画")
                    }
                }
            }
        }
    )
}

/**
 * 漫画网格
 */
@Composable
private fun MangaGrid(
    mangaList: List<Manga>,
    selectionMode: Boolean,
    selectedMangaIds: Set<Long>,
    onMangaClick: (Manga) -> Unit,
    onMangaLongClick: (Manga) -> Unit,
    onFavoriteClick: (Manga) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mangaList) { manga ->
            GridComicCard(
                manga = manga,
                isSelected = manga.id in selectedMangaIds,
                selectionMode = selectionMode,
                onClick = { onMangaClick(manga) },
                onLongClick = { onMangaLongClick(manga) },
                onFavoriteClick = { onFavoriteClick(manga) }
            )
        }
    }
}

/**
 * 加载指示器
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * 错误界面
 */
@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("重试")
            }
        }
    }
}

/**
 * 空界面
 */
@Composable
private fun EmptyScreen(
    searchQuery: String,
    onAddMangaClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (searchQuery.isNotBlank()) Icons.Default.Search else Icons.Outlined.MenuBook,
                contentDescription = if (searchQuery.isNotBlank()) "无搜索结果" else "空书架",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "没有找到 \"$searchQuery\" 相关的漫画"
                } else {
                    "还没有添加任何漫画"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            if (searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAddMangaClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加漫画")
                }
            }
        }
    }
}

/**
 * 排序对话框
 */
@Composable
private fun SortDialog(
    currentSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("排序方式") },
        text = {
            Column {
                SortOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentSort,
                            onClick = { onSortSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (option) {
                                SortOption.TITLE_ASC -> "标题 A-Z"
                                SortOption.TITLE_DESC -> "标题 Z-A"
                                SortOption.DATE_ADDED_ASC -> "添加时间 早-晚"
                                SortOption.DATE_ADDED_DESC -> "添加时间 晚-早"
                                SortOption.LAST_READ_ASC -> "最后阅读 早-晚"
                                SortOption.LAST_READ_DESC -> "最后阅读 晚-早"
                                SortOption.RATING_ASC -> "评分 低-高"
                                SortOption.RATING_DESC -> "评分 高-低"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}

/**
 * 筛选对话框
 */
@Composable
private fun FilterDialog(
    currentFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选条件") },
        text = {
            Column {
                FilterOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == currentFilter,
                            onClick = { onFilterSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (option) {
                                FilterOption.ALL -> "全部"
                                FilterOption.FAVORITES -> "收藏"
                                FilterOption.READING -> "阅读中"
                                FilterOption.COMPLETED -> "已完成"
                                FilterOption.UNREAD -> "未读"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}