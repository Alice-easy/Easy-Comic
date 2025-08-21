package com.easycomic.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.model.ThemePreference
import com.easycomic.domain.usecase.GetThemePreferenceUseCase
import com.easycomic.domain.usecase.UpdateThemePreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 主题管理ViewModel
 */
class ThemeViewModel(
    private val getThemePreferenceUseCase: GetThemePreferenceUseCase,
    private val updateThemePreferenceUseCase: UpdateThemePreferenceUseCase
) : ViewModel() {
    
    private val _themePreference = MutableStateFlow(ThemePreference())
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()
    
    init {
        observeThemePreference()
    }
    
    private fun observeThemePreference() {
        viewModelScope.launch {
            getThemePreferenceUseCase().collect { preference ->
                _themePreference.value = preference
            }
        }
    }
    
    /**
     * 更新主题模式
     */
    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateThemePreferenceUseCase.updateThemeMode(themeMode)
        }
    }
    
    /**
     * 切换动态色彩设置
     */
    fun toggleDynamicColors() {
        viewModelScope.launch {
            updateThemePreferenceUseCase.updateDynamicColors(!_themePreference.value.useDynamicColors)
        }
    }
    
    /**
     * 更新自定义种子颜色
     */
    fun updateCustomSeedColor(color: Long?) {
        viewModelScope.launch {
            updateThemePreferenceUseCase.updateCustomSeedColor(color)
        }
    }
    
    /**
     * 重置到默认设置
     */
    fun resetToDefault() {
        viewModelScope.launch {
            updateThemePreferenceUseCase.resetToDefault()
        }
    }
}
