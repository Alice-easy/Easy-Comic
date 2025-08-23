package com.easycomic.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.model.ThemePreference
import com.easycomic.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 主题偏好设置仓库实现类
 */
class ThemeRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : ThemeRepository {
    
    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val USE_DYNAMIC_COLORS_KEY = booleanPreferencesKey("use_dynamic_colors")
        private val CUSTOM_SEED_COLOR_KEY = longPreferencesKey("custom_seed_color")
    }
    
    override fun getThemePreference(): Flow<ThemePreference> {
        return dataStore.data.map { preferences ->
            ThemePreference(
                themeMode = when (preferences[THEME_MODE_KEY]) {
                    "LIGHT" -> ThemeMode.LIGHT
                    "DARK" -> ThemeMode.DARK
                    else -> ThemeMode.SYSTEM
                },
                useDynamicColors = preferences[USE_DYNAMIC_COLORS_KEY] ?: true,
                customSeedColor = preferences[CUSTOM_SEED_COLOR_KEY]
            )
        }
    }
    
    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }
    
    override suspend fun updateDynamicColors(useDynamicColors: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS_KEY] = useDynamicColors
        }
    }
    
    override suspend fun updateCustomSeedColor(color: Long?) {
        dataStore.edit { preferences ->
            if (color != null) {
                preferences[CUSTOM_SEED_COLOR_KEY] = color
            } else {
                preferences.remove(CUSTOM_SEED_COLOR_KEY)
            }
        }
    }
    
    override suspend fun resetToDefault() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
