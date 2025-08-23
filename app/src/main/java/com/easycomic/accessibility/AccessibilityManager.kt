package com.easycomic.accessibility

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import timber.log.Timber

/**
 * 无障碍支持管理器
 * 
 * 提供完整的无障碍功能，包括屏幕阅读器支持、语音反馈、
 * 大字体支持、高对比度等功能，确保视觉障碍用户能够正常使用应用
 */
object AccessibilityManager {
    
    private const val PREFS_NAME = "accessibility_prefs"
    private const val KEY_FONT_SCALE = "font_scale"
    private const val KEY_HIGH_CONTRAST = "high_contrast"
    
    /**
     * 字体缩放级别
     */
    enum class FontScale(val scale: Float, val displayName: String) {
        SMALL(0.85f, "小"),
        NORMAL(1.0f, "正常"),
        LARGE(1.15f, "大"),
        EXTRA_LARGE(1.3f, "特大"),
        HUGE(1.5f, "超大");
        
        companion object {
            fun fromScale(scale: Float): FontScale {
                return values().minByOrNull { kotlin.math.abs(it.scale - scale) } ?: NORMAL
            }
        }
    }
    
    /**
     * 检查系统无障碍服务状态
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) 
            as android.view.accessibility.AccessibilityManager
        return accessibilityManager.isEnabled
    }
    
    /**
     * 检查是否需要高对比度
     */
    fun isHighContrastEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HIGH_CONTRAST, false)
    }
    
    /**
     * 设置高对比度模式
     */
    fun setHighContrastEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HIGH_CONTRAST, enabled).apply()
        
        Timber.d("High contrast mode: $enabled")
    }
    
    /**
     * 获取字体缩放级别
     */
    fun getFontScale(context: Context): FontScale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val scale = prefs.getFloat(KEY_FONT_SCALE, 1.0f)
        return FontScale.fromScale(scale)
    }
    
    /**
     * 设置字体缩放级别
     */
    fun setFontScale(context: Context, fontScale: FontScale) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_FONT_SCALE, fontScale.scale).apply()
        
        Timber.d("Font scale set to: ${fontScale.displayName} (${fontScale.scale})")
    }
    
    /**
     * 为漫画阅读器生成无障碍描述
     */
    fun generateComicAccessibilityDescription(
        comicTitle: String,
        currentPage: Int,
        totalPages: Int,
        pageDescription: String? = null
    ): String {
        val baseDescription = "漫画《$comicTitle》，第${currentPage}页，共${totalPages}页"
        
        return if (pageDescription != null) {
            "$baseDescription。页面内容：$pageDescription"
        } else {
            baseDescription
        }
    }
    
    /**
     * 为书架项目生成无障碍描述
     */
    fun generateBookshelfItemDescription(
        comicTitle: String,
        readingProgress: Float,
        isFavorite: Boolean,
        lastReadTime: String? = null
    ): String {
        val progressText = "阅读进度${(readingProgress * 100).toInt()}%"
        val favoriteText = if (isFavorite) "，已收藏" else ""
        val timeText = lastReadTime?.let { "，最后阅读时间$it" } ?: ""
        
        return "漫画《$comicTitle》，$progressText$favoriteText$timeText"
    }
}

/**
 * Compose无障碍扩展函数
 */

/**
 * 为Compose组件添加无障碍支持
 */
@Composable
fun Modifier.accessibilitySupport(
    contentDescription: String
): Modifier {
    return this.semantics {
        this.contentDescription = contentDescription
    }
}

/**
 * 获取无障碍友好的字体大小
 */
@Composable
fun getAccessibleFontSize(baseFontSize: Int): Int {
    val context = LocalContext.current
    val fontScale = AccessibilityManager.getFontScale(context)
    return (baseFontSize * fontScale.scale).toInt()
}

/**
 * 无障碍状态数据类
 */
data class AccessibilityState(
    val isEnabled: Boolean,
    val isHighContrastEnabled: Boolean,
    val fontScale: AccessibilityManager.FontScale
)

/**
 * 无障碍状态监听器
 */
@Composable
fun rememberAccessibilityState(): AccessibilityState {
    val context = LocalContext.current
    
    return androidx.compose.runtime.remember {
        AccessibilityState(
            isEnabled = AccessibilityManager.isAccessibilityEnabled(context),
            isHighContrastEnabled = AccessibilityManager.isHighContrastEnabled(context),
            fontScale = AccessibilityManager.getFontScale(context)
        )
    }
}