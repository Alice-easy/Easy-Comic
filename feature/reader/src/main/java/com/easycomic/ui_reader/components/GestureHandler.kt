package com.easycomic.ui_reader.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * 阅读器手势处理器
 * 支持点击翻页、滑动翻页、双击缩放等手势
 */
@Composable
fun ReaderGestureHandler(
    modifier: Modifier = Modifier,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onToggleControls: () -> Unit,
    onDoubleTap: (Offset) -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
    enableSwipeNavigation: Boolean = true,
    enableTapNavigation: Boolean = true,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // 计算屏幕区域
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val tapZoneWidth = screenWidth * 0.3f // 左右各30%区域用于翻页
    
    var isDragging by remember { mutableStateOf(false) }
    var dragStartX by remember { mutableFloatStateOf(0f) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // 处理点击手势
                if (enableTapNavigation) {
                    detectTapGestures(
                        onTap = { offset ->
                            when {
                                offset.x < tapZoneWidth -> onPreviousPage()
                                offset.x > screenWidth - tapZoneWidth -> onNextPage()
                                else -> onToggleControls()
                            }
                        },
                        onDoubleTap = { offset ->
                            onDoubleTap(offset)
                        },
                        onLongPress = { offset ->
                            onLongPress(offset)
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                // 处理滑动手势
                if (enableSwipeNavigation) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragStartX = offset.x
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDrag = { _, dragAmount ->
                            // 可以在这里添加拖拽反馈效果
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                // 处理滑动翻页
                if (enableSwipeNavigation) {
                    detectDragGestures(
                        onDragEnd = {
                            if (isDragging) {
                                val currentX = dragStartX
                                val deltaX = currentX - dragStartX
                                val threshold = screenWidth * 0.2f // 20%的滑动距离触发翻页
                                
                                when {
                                    deltaX > threshold -> onPreviousPage()
                                    deltaX < -threshold -> onNextPage()
                                }
                            }
                            isDragging = false
                        }
                    ) { _, _ -> }
                }
            }
    ) {
        content()
    }
}

/**
 * 手势配置数据类
 */
data class GestureConfig(
    val enableTapNavigation: Boolean = true,
    val enableSwipeNavigation: Boolean = true,
    val enableDoubleTapZoom: Boolean = true,
    val swipeThreshold: Float = 0.2f, // 滑动阈值（屏幕宽度的百分比）
    val tapZoneWidth: Float = 0.3f    // 点击区域宽度（屏幕宽度的百分比）
)

/**
 * 记住手势配置的Composable
 */
@Composable
fun rememberGestureConfig(
    enableTapNavigation: Boolean = true,
    enableSwipeNavigation: Boolean = true,
    enableDoubleTapZoom: Boolean = true,
    swipeThreshold: Float = 0.2f,
    tapZoneWidth: Float = 0.3f
): GestureConfig {
    return remember {
        GestureConfig(
            enableTapNavigation = enableTapNavigation,
            enableSwipeNavigation = enableSwipeNavigation,
            enableDoubleTapZoom = enableDoubleTapZoom,
            swipeThreshold = swipeThreshold,
            tapZoneWidth = tapZoneWidth
        )
    }
}

/**
 * 手势反馈效果
 */
@Composable
fun GestureFeedback(
    isVisible: Boolean,
    feedbackType: FeedbackType,
    modifier: Modifier = Modifier
) {
    // 可以在这里添加手势反馈的视觉效果
    // 比如翻页动画、缩放提示等
}

/**
 * 反馈类型枚举
 */
enum class FeedbackType {
    NEXT_PAGE,
    PREVIOUS_PAGE,
    ZOOM_IN,
    ZOOM_OUT,
    CONTROLS_TOGGLE
}