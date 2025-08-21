package com.easycomic.ui_reader

/**
 * 阅读方向枚举
 */
enum class ReadingDirection {
    /** 水平阅读（左右翻页） */
    HORIZONTAL,
    /** 垂直阅读（上下滚动） */
    VERTICAL
}

/**
 * 阅读模式枚举
 */
enum class ReadingMode {
    /** 适应屏幕 */
    FIT,
    /** 填充屏幕 */
    FILL,
    /** 原始尺寸 */
    ORIGINAL
}

/**
 * 阅读器设置
 */
data class ReaderSettings(
    val readingDirection: ReadingDirection = ReadingDirection.HORIZONTAL,
    val readingMode: ReadingMode = ReadingMode.FIT,
    val isMenuVisible: Boolean = false,
    val enableZoom: Boolean = true,
    val enableDoubleTapZoom: Boolean = true,
    val zoomLevel: Float = 1.0f,
    val brightnessLevel: Float = -1f, // -1表示使用系统亮度
    val keepScreenOn: Boolean = true
)
