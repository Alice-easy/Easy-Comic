package com.easycomic.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF8FF7D0),
    onPrimary = Color(0xFF00382A),
    primaryContainer = Color(0xFF00513D),
    onPrimaryContainer = Color(0xFF8FF7D0),
    secondary = Color(0xFFB3CCC2),
    onSecondary = Color(0xFF1D352E),
    secondaryContainer = Color(0xFF334C44),
    onSecondaryContainer = Color(0xFFCFE9DE),
    tertiary = Color(0xFF8FF7D0),
    onTertiary = Color(0xFF00382A),
    tertiaryContainer = Color(0xFF00513D),
    onTertiaryContainer = Color(0xFF8FF7D0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E938F),
    background = Color(0xFF191C1B),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF191C1B),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C3),
    inverseSurface = Color(0xFFE1E3E0),
    inverseOnSurface = Color(0xFF2D322F),
    inversePrimary = Color(0xFF006C50)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C50),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8FF7D0),
    onPrimaryContainer = Color(0xFF002116),
    secondary = Color(0xFF4C635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCFE9DE),
    onSecondaryContainer = Color(0xFF072019),
    tertiary = Color(0xFF006C50),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF8FF7D0),
    onTertiaryContainer = Color(0xFF002116),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF6F7974),
    background = Color(0xFFFBFDF9),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFBFDF9),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDCE5DE),
    onSurfaceVariant = Color(0xFF404944),
    inverseSurface = Color(0xFF2D322F),
    inverseOnSurface = Color(0xFFF0F1EE),
    inversePrimary = Color(0xFF7FDAB4)
)

@Composable
fun EasyComicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}