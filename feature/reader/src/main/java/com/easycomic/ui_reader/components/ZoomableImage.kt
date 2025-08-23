package com.easycomic.ui_reader.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * 可缩放的图片组件
 * 支持双击缩放、捏合缩放、拖拽移动等手势操作
 */
@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    onDoubleTap: ((Offset) -> Unit)? = null,
    onImageLoad: (() -> Unit)? = null,
    onImageError: (() -> Unit)? = null
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    
    val density = LocalDensity.current
    
    // 重置缩放和偏移
    fun resetTransform() {
        scale = 1f
        offset = Offset.Zero
    }
    
    // 限制偏移范围
    fun constrainOffset(newOffset: Offset, imageSize: IntSize, containerSize: IntSize): Offset {
        if (scale <= 1f) return Offset.Zero
        
        val scaledWidth = imageSize.width * scale
        val scaledHeight = imageSize.height * scale
        
        val maxOffsetX = max(0f, (scaledWidth - containerSize.width) / 2f)
        val maxOffsetY = max(0f, (scaledHeight - containerSize.height) / 2f)
        
        return Offset(
            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }
    
    // 双击缩放处理
    fun handleDoubleTap(tapOffset: Offset) {
        if (scale > 1f) {
            // 如果已经缩放，则重置
            resetTransform()
        } else {
            // 缩放到双击位置
            val targetScale = min(maxScale, 2.5f)
            scale = targetScale
            
            // 计算偏移，使双击点居中
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val newOffset = Offset(
                x = (centerX - tapOffset.x) * (targetScale - 1f),
                y = (centerY - tapOffset.y) * (targetScale - 1f)
            )
            offset = constrainOffset(newOffset, IntSize(size.width, size.height), size)
        }
        onDoubleTap?.invoke(tapOffset)
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .pointerInput(Unit) {
                    detectTransformGestures(
                        panZoomLock = true
                    ) { _, pan, zoom, _ ->
                        // 处理缩放
                        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                        
                        // 处理拖拽
                        val newOffset = if (newScale > 1f) {
                            offset + pan
                        } else {
                            Offset.Zero
                        }
                        
                        scale = newScale
                        offset = constrainOffset(
                            newOffset,
                            IntSize(this@pointerInput.size.width, this@pointerInput.size.height),
                            IntSize(this@pointerInput.size.width, this@pointerInput.size.height)
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            handleDoubleTap(tapOffset)
                        }
                    )
                },
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Text(
                        text = "图片加载失败",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            onSuccess = { onImageLoad?.invoke() },
            onError = { onImageError?.invoke() }
        )
    }
    
    // 记录容器大小
    LaunchedEffect(size) {
        // 当容器大小改变时，重新约束偏移
        offset = constrainOffset(offset, IntSize(size.width, size.height), size)
    }
}

/**
 * 缩放状态数据类
 */
data class ZoomState(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val isZoomed: Boolean = scale > 1f
)

/**
 * 记住缩放状态的Composable
 */
@Composable
fun rememberZoomState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero
): MutableState<ZoomState> {
    return remember {
        mutableStateOf(
            ZoomState(
                scale = initialScale,
                offset = initialOffset
            )
        )
    }
}