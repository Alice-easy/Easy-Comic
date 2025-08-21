package com.easycomic.domain.usecase

import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.repository.ThemeRepository

/**
 * 更新主题偏好设置的用例
 */
class UpdateThemePreferenceUseCase(
    private val themeRepository: ThemeRepository
) {
    
    /**
     * 更新主题模式
     */
    suspend fun updateThemeMode(themeMode: ThemeMode) {
        themeRepository.updateThemeMode(themeMode)
    }
    
    /**
     * 更新动态色彩设置
     */
    suspend fun updateDynamicColors(useDynamicColors: Boolean) {
        themeRepository.updateDynamicColors(useDynamicColors)
    }
    
    /**
     * 更新自定义种子颜色
     */
    suspend fun updateCustomSeedColor(color: Long?) {
        themeRepository.updateCustomSeedColor(color)
    }
    
    /**
     * 重置到默认设置
     */
    suspend fun resetToDefault() {
        themeRepository.resetToDefault()
    }
}
