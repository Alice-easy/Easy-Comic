package com.easycomic.domain.model

/**
 * 表示用户主题偏好的数据类
 */
data class ThemePreference(
    /**
     * 主题模式
     */
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    
    /**
     * 是否启用动态色彩（Material You）
     */
    val useDynamicColors: Boolean = true,
    
    /**
     * 自定义主题色彩（当不使用动态色彩时）
     */
    val customSeedColor: Long? = null
)

/**
 * 主题模式枚举
 */
enum class ThemeMode {
    /**
     * 跟随系统设置
     */
    SYSTEM,
    
    /**
     * 强制亮色模式
     */
    LIGHT,
    
    /**
     * 强制暗色模式
     */
    DARK
}
