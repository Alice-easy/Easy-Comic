package com.easycomic.ui_bookshelf.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.easycomic.ui_bookshelf.BookshelfViewModel
import com.easycomic.ui_bookshelf.R

/**
 * 书架顶部应用栏组件
 * 
 * 重构优化点：
 * 1. 从BookshelfScreen中提取出来，单一职责
 * 2. 状态管理更清晰
 * 3. 可复用性更强
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfTopBar(
    isSearchMode: Boolean,
    searchQuery: String,
    sortOrder: BookshelfViewModel.SortOrder,
    isLoading: Boolean,
    isImporting: Boolean,
    onSearchModeChanged: (Boolean) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSortOrderChanged: (BookshelfViewModel.SortOrder) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    if (isSearchMode) {
        SearchTopAppBar(
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            onSearchDismissed = {
                onSearchModeChanged(false)
                onSearchQueryChanged("")
            }
        )
    } else {
        TopAppBar(
            title = { Text(stringResource(id = R.string.bookshelf_title)) },
            actions = {
                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading && !isImporting
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
                IconButton(onClick = { onSearchModeChanged(true) }) {
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
                        onSortOrderChanged(order)
                        showSortMenu = false
                    },
                    onDismiss = { showSortMenu = false }
                )
            }
        )
    }
}

/**
 * 搜索模式顶部应用栏
 */
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

/**
 * 选择模式顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopAppBar(
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

/**
 * 排序下拉菜单
 */
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
        SortMenuItem(
            text = "标题 A-Z",
            isSelected = currentSortOrder == BookshelfViewModel.SortOrder.TITLE_ASC,
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.TITLE_ASC) }
        )
        SortMenuItem(
            text = "标题 Z-A",
            isSelected = currentSortOrder == BookshelfViewModel.SortOrder.TITLE_DESC,
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.TITLE_DESC) }
        )
        SortMenuItem(
            text = "添加时间 (最新)",
            isSelected = currentSortOrder == BookshelfViewModel.SortOrder.DATE_ADDED_DESC,
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.DATE_ADDED_DESC) }
        )
        SortMenuItem(
            text = "最近阅读",
            isSelected = currentSortOrder == BookshelfViewModel.SortOrder.LAST_READ_DESC,
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.LAST_READ_DESC) }
        )
        SortMenuItem(
            text = "阅读进度",
            isSelected = currentSortOrder == BookshelfViewModel.SortOrder.PROGRESS_DESC,
            onClick = { onSortOrderSelected(BookshelfViewModel.SortOrder.PROGRESS_DESC) }
        )
    }
}

/**
 * 排序菜单项
 */
@Composable
private fun SortMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = onClick,
        trailingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, contentDescription = null) }
        } else null
    )
}