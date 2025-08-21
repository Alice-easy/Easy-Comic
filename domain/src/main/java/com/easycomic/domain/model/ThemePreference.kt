package com.easycomic.domain.model

/**
 * 主题偏好设置数据类
 */
data class ThemePreference(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColors: Boolean = true,
    val customSeedColor: Long? = null
)