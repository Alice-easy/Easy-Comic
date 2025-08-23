package com.easycomic.ui_bookshelf.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.easycomic.domain.model.Manga

/**
 * 优化后的漫画卡片组件
 * 
 * 重构优化点：
 * 1. 拆分为更小的可复用组件
 * 2. 提取公共逻辑到独立函数
 * 3. 减少重复代码
 * 4. 提高可测试性和可维护性
 */

/**
 * 列表样式漫画卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizedComicCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                if (selectionMode) onLongClick() else onClick()
            }
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 封面区域
            CoverImageSection(
                manga = manga,
                isSelected = isSelected,
                selectionMode = selectionMode,
                onLongClick = onLongClick
            )
            
            // 信息区域
            MangaInfoSection(
                manga = manga,
                searchQuery = searchQuery
            )
        }
    }
}

/**
 * 网格样式漫画卡片
 */
@Composable
fun OptimizedGridComicCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    searchQuery: String = "",
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clickable { 
                if (selectionMode) onLongClick() else onClick()
            }
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 封面图片
            CoverImage(
                coverImagePath = manga.coverImagePath,
                title = manga.title,
                modifier = Modifier.fillMaxSize()
            )
            
            // 状态指示器
            StatusIndicators(
                manga = manga,
                isSelected = isSelected,
                selectionMode = selectionMode
            )
            
            // 底部标题
            GridCardTitle(
                title = manga.title,
                searchQuery = searchQuery,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 封面图片区域组件
 */
@Composable
private fun CoverImageSection(
    manga: Manga,
    isSelected: Boolean,
    selectionMode: Boolean,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .clickable { onLongClick() }
    ) {
        CoverImage(
            coverImagePath = manga.coverImagePath,
            title = manga.title,
            modifier = Modifier.fillMaxSize()
        )
        
        StatusIndicators(
            manga = manga,
            isSelected = isSelected,
            selectionMode = selectionMode
        )
        
        // 阅读进度条
        if (manga.progressPercentage > 0f && manga.progressPercentage < 100f) {
            ProgressIndicator(
                progress = manga.progressPercentage,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/**
 * 封面图片组件
 */
@Composable
private fun CoverImage(
    coverImagePath: String,
    title: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverImagePath)
            .crossfade(true)
            .build(),
        contentDescription = title,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop,
        placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        ),
        error = androidx.compose.ui.graphics.painter.ColorPainter(
            MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        )
    )
}

/**
 * 状态指示器组件
 */
@Composable
private fun StatusIndicators(
    manga: Manga,
    isSelected: Boolean,
    selectionMode: Boolean
) {
    // 选择状态指示器
    if (selectionMode) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            modifier = Modifier
                .padding(8.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        )
    }
    
    // 收藏状态指示器
    if (manga.isFavorite) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "收藏",
            modifier = Modifier.padding(8.dp),
            tint = Color.Red
        )
    }
}

/**
 * 进度指示器组件
 */
@Composable
private fun ProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearProgressIndicator(
        progress = { progress / 100f },
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
    )
}

/**
 * 漫画信息区域组件
 */
@Composable
private fun MangaInfoSection(
    manga: Manga,
    searchQuery: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // 标题
        HighlightedText(
            text = manga.title,
            searchQuery = searchQuery,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 作者
        if (manga.author.isNotBlank()) {
            HighlightedText(
                text = manga.author,
                searchQuery = searchQuery,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // 底部信息行
        MangaBottomInfo(manga = manga)
    }
}

/**
 * 网格卡片标题组件
 */
@Composable
private fun GridCardTitle(
    title: String,
    searchQuery: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    .compositeOver(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            .padding(8.dp)
    ) {
        HighlightedText(
            text = title,
            searchQuery = searchQuery,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 底部信息行组件
 */
@Composable
private fun MangaBottomInfo(manga: Manga) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 页码信息
        Text(
            text = "${manga.currentPage}/${manga.pageCount}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        // 评分
        if (manga.rating > 0f) {
            RatingDisplay(rating = manga.rating)
        }
    }
}

/**
 * 评分显示组件
 */
@Composable
private fun RatingDisplay(rating: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = "评分",
            tint = Color(0xFFFFA000),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

/**
 * 高亮文本组件
 */
@Composable
private fun HighlightedText(
    text: String,
    searchQuery: String,
    style: androidx.compose.ui.text.TextStyle,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight? = null,
    maxLines: Int = Int.MAX_VALUE,
    textAlign: TextAlign? = null
) {
    Text(
        text = createHighlightedText(text, searchQuery),
        modifier = Modifier.fillMaxWidth(),
        style = style,
        fontWeight = fontWeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        color = color
    )
}

/**
 * 创建高亮文本的工具函数
 */
private fun createHighlightedText(
    text: String,
    searchQuery: String,
    highlightColor: Color = Color(0x4D2196F3) // 半透明蓝色
): AnnotatedString {
    return if (searchQuery.isBlank()) {
        AnnotatedString(text)
    } else {
        buildAnnotatedString {
            val query = searchQuery.lowercase()
            val lowerText = text.lowercase()
            var currentIndex = 0
            
            while (currentIndex < text.length) {
                val matchIndex = lowerText.indexOf(query, currentIndex)
                if (matchIndex == -1) {
                    append(text.substring(currentIndex))
                    break
                } else {
                    if (matchIndex > currentIndex) {
                        append(text.substring(currentIndex, matchIndex))
                    }
                    withStyle(
                        SpanStyle(
                            background = highlightColor,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(text.substring(matchIndex, matchIndex + query.length))
                    }
                    currentIndex = matchIndex + query.length
                }
            }
        }
    }
}