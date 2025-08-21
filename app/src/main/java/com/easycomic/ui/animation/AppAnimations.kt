package com.easycomic.ui.animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 应用级动画配置
 */
object AppAnimations {
    
    /**
     * 标准缓动曲线
     */
    val StandardEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    
    /**
     * 强调缓动曲线  
     */
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    /**
     * 标准动画时长
     */
    const val StandardDuration = 300
    
    /**
     * 强调动画时长
     */
    const val EmphasizedDuration = 500
    
    /**
     * 页面进入动画
     */
    fun slideInFromRight(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        )
    }
    
    /**
     * 页面退出动画
     */
    fun slideOutToRight(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        )
    }
    
    /**
     * 页面退出到左侧动画
     */
    fun slideOutToLeft(): ExitTransition {
        return slideOutHorizontally(
            targetOffsetX = { -it },
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        )
    }
    
    /**
     * 页面从左侧进入动画
     */
    fun slideInFromLeft(): EnterTransition {
        return slideInHorizontally(
            initialOffsetX = { -it },
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = StandardDuration,
                easing = StandardEasing
            )
        )
    }
    
    /**
     * 主题切换动画规格
     */
    fun themeTransitionSpec(): AnimationSpec<Float> = tween(
        durationMillis = EmphasizedDuration,
        easing = EmphasizedEasing
    )
    
    /**
     * 设置项展开/收起动画
     */
    fun expandVertically(): EnterTransition = expandVertically(
        animationSpec = tween(
            durationMillis = StandardDuration,
            easing = StandardEasing
        )
    )
    
    fun shrinkVertically(): ExitTransition = shrinkVertically(
        animationSpec = tween(
            durationMillis = StandardDuration,
            easing = StandardEasing
        )
    )
    
    /**
     * 浮动操作按钮缩放动画
     */
    fun fabScale(): AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * 卡片悬停效果
     */
    fun cardElevation(): AnimationSpec<Dp> = tween(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    )
}

/**
 * 可组合的动画包装器
 */
@Composable
fun AnimatedContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AppAnimations.StandardDuration,
                easing = AppAnimations.StandardEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 4 },
            animationSpec = tween(
                durationMillis = AppAnimations.StandardDuration,
                easing = AppAnimations.StandardEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = AppAnimations.StandardDuration,
                easing = AppAnimations.StandardEasing
            )
        ) + slideOutVertically(
            targetOffsetY = { -it / 4 },
            animationSpec = tween(
                durationMillis = AppAnimations.StandardDuration,
                easing = AppAnimations.StandardEasing
            )
        )
    ) {
        content()
    }
}
