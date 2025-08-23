package com.easycomic.domain.repository

import com.easycomic.domain.model.ThemePreference
import kotlinx.coroutines.flow.Flow

/**
 * 主题偏好设置仓库接口
 */
interface ThemeRepository {
    
    /**
     * 获取当前主题偏好设置的流
     */
    fun getThemePreference(): Flow<ThemePreference>
    
    /**
     * 更新主题模式
     */
    suspend fun updateThemeMode(themeMode: com.easycomic.domain.model.ThemeMode)
    
    /**
     * 更新动态色彩设置
     */
    suspend fun updateDynamicColors(useDynamicColors: Boolean)
    
    /**
     * 更新自定义种子颜色
     */
    suspend fun updateCustomSeedColor(color: Long?)
    
    /**
     * 重置到默认设置
     */
    suspend fun resetToDefault()
}
