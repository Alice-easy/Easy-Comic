package com.easycomic.ui_bookshelf.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.easycomic.domain.model.Manga
import com.easycomic.ui_bookshelf.GridComicCard

/**
 * 书架内容组件
 * 
 * 重构优化点：
 * 1. 从BookshelfScreen中提取内容显示逻辑
 * 2. 统一处理加载、空状态、内容显示
 * 3. 提高组件复用性和可测试性
 */
@Composable
fun BookshelfContent(
    mangas: List<Manga>,
    searchQuery: String,
    isLoading: Boolean,
    selectionMode: Boolean,
    selectedMangas: Set<Long>,
    onMangaClick: (Long) -> Unit,
    onMangaLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            LoadingState(modifier = modifier)
        }
        mangas.isEmpty() -> {
            EmptyState(
                message = if (searchQuery.isNotBlank()) {
                    "未找到匹配的漫画"
                } else {
                    "还没有漫画，点击右下角按钮导入"
                },
                modifier = modifier
            )
        }
        else -> {
            MangaGrid(
                mangas = mangas,
                searchQuery = searchQuery,
                selectionMode = selectionMode,
                selectedMangas = selectedMangas,
                onMangaClick = onMangaClick,
                onMangaLongClick = onMangaLongClick,
                modifier = modifier
            )
        }
    }
}

/**
 * 漫画网格显示
 */
@Composable
private fun MangaGrid(
    mangas: List<Manga>,
    searchQuery: String,
    selectionMode: Boolean,
    selectedMangas: Set<Long>,
    onMangaClick: (Long) -> Unit,
    onMangaLongClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(128.dp),
        modifier = modifier.padding(8.dp),
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
                        onMangaLongClick(manga.id)
                    } else {
                        onMangaClick(manga.id)
                    }
                },
                onLongClick = { 
                    onMangaLongClick(manga.id)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 加载状态组件
 */
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "正在加载漫画...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * 空状态组件
 */
@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}