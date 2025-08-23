package com.easycomic.ui_bookshelf

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
 * 创建带高亮效果的文本
 */
@Composable
private fun highlightText(
    text: String,
    searchQuery: String,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleSmall,
    color: Color = MaterialTheme.colorScheme.onSurface,
    highlightColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                    // 没有更多匹配，添加剩余文本
                    append(text.substring(currentIndex))
                    break
                } else {
                    // 添加匹配前的文本
                    if (matchIndex > currentIndex) {
                        append(text.substring(currentIndex, matchIndex))
                    }
                    // 添加高亮的匹配文本
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

/**
 * 漫画卡片组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    searchQuery: String = "", // 新增搜索关键词参数
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                if (selectionMode) {
                    onLongClick()
                } else {
                    onClick()
                }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onLongClick() }
        ) {
            // 封面图片
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(manga.coverImagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = manga.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                    error = androidx.compose.ui.graphics.painter.ColorPainter(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
                
                // 选择状态指示器
                if (selectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
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
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        tint = Color.Red
                    )
                }
                
                // 阅读进度指示器
                if (manga.progressPercentage > 0f && manga.progressPercentage < 100f) {
                    LinearProgressIndicator(
                        progress = { manga.progressPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
            }
            
            // 漫画信息
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 标题
                Text(
                    text = highlightText(
                        text = manga.title,
                        searchQuery = searchQuery,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 作者
                if (manga.author.isNotBlank()) {
                    Text(
                        text = highlightText(
                            text = manga.author,
                            searchQuery = searchQuery,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 底部信息行
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
                                text = String.format("%.1f", manga.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 网格项漫画卡片（适配网格布局）
 */
@Composable
fun GridComicCard(
    manga: Manga,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    selectionMode: Boolean = false,
    searchQuery: String = "", // 新增搜索关键词参数
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .aspectRatio(3f / 4f)
            .clickable { 
                if (selectionMode) {
                    onLongClick()
                } else {
                    onClick()
                }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onLongClick() }
        ) {
            // 封面图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(manga.coverImagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = manga.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                ),
                error = androidx.compose.ui.graphics.painter.ColorPainter(
                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            )
            
            // 选择状态指示器
            if (selectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
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
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                        tint = Color.Red
                    )
                }
                
                // 阅读进度指示器
                if (manga.progressPercentage > 0f && manga.progressPercentage < 100f) {
                    LinearProgressIndicator(
                        progress = { manga.progressPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(3.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                }
                
                // 标题（底部显示）
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                            .compositeOver(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = highlightText(
                        text = manga.title,
                        searchQuery = searchQuery,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}