package com.easycomic.domain.usecase

import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.model.ThemePreference
import com.easycomic.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * 主题相关用例聚合类
 * 
 * 负责处理应用主题相关的业务逻辑，包括主题模式切换、动态颜色配置、
 * 自定义种子颜色设置等功能。
 * 
 * @property themeRepository 主题数据仓库，用于持久化主题配置
 * 
 * @author EasyComic Team
 * @since 1.0.0
 */
class ThemeUseCases(
    private val themeRepository: ThemeRepository
) {
    
    /**
     * 获取当前主题偏好设置
     * 
     * @return Flow<ThemePreference> 主题偏好设置的数据流
     */
    fun getThemePreference(): Flow<ThemePreference> = themeRepository.getThemePreference()
    
    /**
     * 更新主题模式
     * 
     * @param themeMode 要设置的主题模式（系统跟随、浅色、深色）
     */
    suspend fun updateThemeMode(themeMode: ThemeMode) = themeRepository.updateThemeMode(themeMode)
    
    /**
     * 更新动态颜色设置
     * 
     * @param useDynamicColors 是否启用动态颜色（Material You）
     */
    suspend fun updateDynamicColors(useDynamicColors: Boolean) = themeRepository.updateDynamicColors(useDynamicColors)
    
    /**
     * 更新自定义种子颜色
     * 
     * @param color 自定义种子颜色值，null表示使用默认颜色
     */
    suspend fun updateCustomSeedColor(color: Long?) = themeRepository.updateCustomSeedColor(color)
    
    /**
     * 重置主题设置为默认值
     * 
     * 将所有主题相关设置恢复到应用初始状态
     */
    suspend fun resetToDefault() = themeRepository.resetToDefault()
    
    /**
     * 切换主题模式
     * 
     * 按照 系统跟随 -> 浅色 -> 深色 -> 系统跟随 的循环顺序切换主题模式
     */
    suspend fun toggleThemeMode() {
        val currentPreference = themeRepository.getThemePreference().first()
        val nextMode = when (currentPreference.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        updateThemeMode(nextMode)
    }
}
