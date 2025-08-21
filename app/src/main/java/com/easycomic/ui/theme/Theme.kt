package com.easycomic.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.easycomic.domain.model.ThemeMode
import com.easycomic.domain.model.ThemePreference
import org.koin.androidx.compose.koinViewModel

/**
 * 应用主题，支持动态主题和用户偏好设置
 */
@Composable
fun EasyComicTheme(
    // 传入的参数，用于向后兼容或特殊情况
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) {
    val themeViewModel: ThemeViewModel = koinViewModel()
    val themePreference by themeViewModel.themePreference.collectAsState()
    
    EasyComicThemeWithPreference(
        themePreference = themePreference,
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

/**
 * 根据主题偏好设置应用主题
 */
@Composable
fun EasyComicThemeWithPreference(
    themePreference: ThemePreference,
    darkTheme: Boolean? = null,
    dynamicColor: Boolean? = null,
    content: @Composable () -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    
    // 根据用户偏好确定是否使用暗色主题
    val shouldUseDarkTheme = darkTheme ?: when (themePreference.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemInDarkTheme
    }
    
    // 根据用户偏好确定是否使用动态色彩
    val shouldUseDynamicColor = dynamicColor ?: themePreference.useDynamicColors
    
    val colorScheme = when {
        shouldUseDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (shouldUseDarkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        shouldUseDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !shouldUseDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}