package com.easycomic.ui_reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import kotlin.math.max
import kotlin.math.min

/**
 * 优化后的可缩放图片组件
 * 
 * 重构优化点：
 * 1. 拆分手势处理逻辑到独立的状态管理类
 * 2. 提取UI组件到独立的可复用组件
 * 3. 简化主组件的复杂度
 * 4. 提高可测试性和可维护性
 */

/**
 * 缩放状态管理类
 */
@Stable
class ZoomableImageState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
    private val minScale: Float = 1f,
    private val maxScale: Float = 5f
) {
    var scale by mutableFloatStateOf(initialScale)
        private set
    
    var offset by mutableStateOf(initialOffset)
        private set
    
    var containerSize by mutableStateOf(IntSize.Zero)
        private set
    
    val isZoomed: Boolean get() = scale > 1f
    
    /**
     * 重置缩放和偏移
     */
    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }
    
    /**
     * 处理双击缩放
     */
    fun handleDoubleTap(tapOffset: Offset) {
        if (scale > 1f) {
            reset()
        } else {
            val targetScale = min(maxScale, 2.5f)
            scale = targetScale
            
            val centerX = containerSize.width / 2f
            val centerY = containerSize.height / 2f
            val newOffset = Offset(
                x = (centerX - tapOffset.x) * (targetScale - 1f),
                y = (centerY - tapOffset.y) * (targetScale - 1f)
            )
            offset = constrainOffset(newOffset)
        }
    }
    
    /**
     * 处理变换手势
     */
    fun handleTransform(pan: Offset, zoom: Float) {
        val newScale = (scale * zoom).coerceIn(minScale, maxScale)
        val newOffset = if (newScale > 1f) {
            offset + pan
        } else {
            Offset.Zero
        }
        
        scale = newScale
        offset = constrainOffset(newOffset)
    }
    
    /**
     * 更新容器大小
     */
    fun updateContainerSize(size: IntSize) {
        containerSize = size
        offset = constrainOffset(offset)
    }
    
    /**
     * 限制偏移范围
     */
    private fun constrainOffset(newOffset: Offset): Offset {
        if (scale <= 1f) return Offset.Zero
        
        val scaledWidth = containerSize.width * scale
        val scaledHeight = containerSize.height * scale
        
        val maxOffsetX = max(0f, (scaledWidth - containerSize.width) / 2f)
        val maxOffsetY = max(0f, (scaledHeight - containerSize.height) / 2f)
        
        return Offset(
            x = newOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = newOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }
}

/**
 * 记住缩放状态
 */
@Composable
fun rememberZoomableImageState(
    initialScale: Float = 1f,
    initialOffset: Offset = Offset.Zero,
    minScale: Float = 1f,
    maxScale: Float = 5f
): ZoomableImageState {
    return remember {
        ZoomableImageState(
            initialScale = initialScale,
            initialOffset = initialOffset,
            minScale = minScale,
            maxScale = maxScale
        )
    }
}

/**
 * 优化后的可缩放图片组件
 */
@Composable
fun OptimizedZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    state: ZoomableImageState = rememberZoomableImageState(),
    onDoubleTap: ((Offset) -> Unit)? = null,
    onImageLoad: (() -> Unit)? = null,
    onImageError: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        ZoomableImageContent(
            imageUrl = imageUrl,
            contentDescription = contentDescription,
            state = state,
            onDoubleTap = { tapOffset ->
                state.handleDoubleTap(tapOffset)
                onDoubleTap?.invoke(tapOffset)
            },
            onImageLoad = onImageLoad,
            onImageError = onImageError
        )
    }
    
    // 监听容器大小变化
    LaunchedEffect(state.containerSize) {
        // 容器大小变化时重新约束偏移
    }
}

/**
 * 可缩放图片内容组件
 */
@Composable
private fun ZoomableImageContent(
    imageUrl: String,
    contentDescription: String?,
    state: ZoomableImageState,
    onDoubleTap: (Offset) -> Unit,
    onImageLoad: (() -> Unit)?,
    onImageError: (() -> Unit)?
) {
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = state.scale,
                scaleY = state.scale,
                translationX = state.offset.x,
                translationY = state.offset.y
            )
            .addZoomGestures(
                state = state,
                onDoubleTap = onDoubleTap
            ),
        contentScale = ContentScale.Fit,
        loading = { LoadingIndicator() },
        error = { ErrorIndicator() },
        onSuccess = { onImageLoad?.invoke() },
        onError = { onImageError?.invoke() }
    )
}

/**
 * 添加缩放手势的修饰符扩展
 */
private fun Modifier.addZoomGestures(
    state: ZoomableImageState,
    onDoubleTap: (Offset) -> Unit
): Modifier = this
    .pointerInput(Unit) {
        detectTransformGestures(
            panZoomLock = true
        ) { _, pan, zoom, _ ->
            state.handleTransform(pan, zoom)
        }
    }
    .pointerInput(Unit) {
        detectTapGestures(
            onDoubleTap = onDoubleTap
        )
    }
    .pointerInput(Unit) {
        // 更新容器大小
        state.updateContainerSize(IntSize(size.width, size.height))
    }

/**
 * 加载指示器组件
 */
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * 错误指示器组件
 */
@Composable
private fun ErrorIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "图片加载失败",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 缩放控制器组件（可选的UI控制器）
 */
@Composable
fun ZoomControls(
    state: ZoomableImageState,
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit = { 
        state.handleTransform(Offset.Zero, 1.2f)
    },
    onZoomOut: () -> Unit = { 
        state.handleTransform(Offset.Zero, 0.8f)
    },
    onReset: () -> Unit = { 
        state.reset()
    }
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 可以添加缩放控制按钮
        // 这里预留接口，具体实现可以根据需要添加
    }
}