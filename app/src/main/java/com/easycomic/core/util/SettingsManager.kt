package com.easycomic.core.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "easy_comic_preferences")

@Singleton
class SettingsManager @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val READER_DIRECTION = stringPreferencesKey("reader_direction")
        private val DEFAULT_ZOOM_MODE = stringPreferencesKey("default_zoom_mode")
        private val WEBDAV_URL = stringPreferencesKey("webdav_url")
        private val WEBDAV_USERNAME = stringPreferencesKey("webdav_username")
        private val WEBDAV_PASSWORD = stringPreferencesKey("webdav_password")
        private val WEBDAV_ENABLED = booleanPreferencesKey("webdav_enabled")
        private val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
    }
    
    private val dataStore = context.dataStore
    
    // Theme settings
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
    
    // Reader settings
    val readerDirection: Flow<String> = dataStore.data.map { preferences ->
        preferences[READER_DIRECTION] ?: "left_to_right"
    }
    
    suspend fun setReaderDirection(direction: String) {
        dataStore.edit { preferences ->
            preferences[READER_DIRECTION] = direction
        }
    }
    
    val defaultZoomMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[DEFAULT_ZOOM_MODE] ?: "fit_to_screen"
    }
    
    suspend fun setDefaultZoomMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_ZOOM_MODE] = mode
        }
    }
    
    // WebDAV settings
    val webDavUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[WEBDAV_URL] ?: ""
    }
    
    suspend fun setWebDavUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[WEBDAV_URL] = url
        }
    }
    
    val webDavUsername: Flow<String> = dataStore.data.map { preferences ->
        preferences[WEBDAV_USERNAME] ?: ""
    }
    
    suspend fun setWebDavUsername(username: String) {
        dataStore.edit { preferences ->
            preferences[WEBDAV_USERNAME] = username
        }
    }
    
    val webDavPassword: Flow<String> = dataStore.data.map { preferences ->
        preferences[WEBDAV_PASSWORD] ?: ""
    }
    
    suspend fun setWebDavPassword(password: String) {
        dataStore.edit { preferences ->
            preferences[WEBDAV_PASSWORD] = password
        }
    }
    
    val webDavEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[WEBDAV_ENABLED] ?: false
    }
    
    suspend fun setWebDavEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WEBDAV_ENABLED] = enabled
        }
    }
    
    val lastSyncTimestamp: Flow<Long> = dataStore.data.map { preferences ->
        preferences[LAST_SYNC_TIMESTAMP] ?: 0L
    }
    
    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIMESTAMP] = timestamp
        }
    }
    
    // Utility functions
    suspend fun getWebDavSettings(): WebDavSettings {
        return WebDavSettings(
            url = webDavUrl.first(),
            username = webDavUsername.first(),
            password = webDavPassword.first(),
            enabled = webDavEnabled.first()
        )
    }
    
    suspend fun setWebDavSettings(settings: WebDavSettings) {
        dataStore.edit { preferences ->
            preferences[WEBDAV_URL] = settings.url
            preferences[WEBDAV_USERNAME] = settings.username
            preferences[WEBDAV_PASSWORD] = settings.password
            preferences[WEBDAV_ENABLED] = settings.enabled
        }
    }
    
    suspend fun clearWebDavSettings() {
        dataStore.edit { preferences ->
            preferences.remove(WEBDAV_URL)
            preferences.remove(WEBDAV_USERNAME)
            preferences.remove(WEBDAV_PASSWORD)
            preferences[WEBDAV_ENABLED] = false
        }
    }
    
    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    data class WebDavSettings(
        val url: String,
        val username: String,
        val password: String,
        val enabled: Boolean
    )
}