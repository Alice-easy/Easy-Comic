package com.easycomic.domain.usecase

import com.easycomic.domain.model.ThemePreference
import com.easycomic.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

/**
 * 获取主题偏好设置的用例
 */
class GetThemePreferenceUseCase(
    private val themeRepository: ThemeRepository
) {
    operator fun invoke(): Flow<ThemePreference> {
        return themeRepository.getThemePreference()
    }
}
