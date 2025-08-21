package com.easycomic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 品牌主色彩定义
 */
object BrandColors {
    val Primary = Color(0xFF6B48FF)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFEDDDFF)
    val OnPrimaryContainer = Color(0xFF21005D)
    
    val Secondary = Color(0xFF625B71)
    val OnSecondary = Color.White
    val SecondaryContainer = Color(0xFFE8DEF8)
    val OnSecondaryContainer = Color(0xFF1D192B)
    
    val Tertiary = Color(0xFF7D5260)
    val OnTertiary = Color.White
    val TertiaryContainer = Color(0xFFFFD8E4)
    val OnTertiaryContainer = Color(0xFF31111D)
    
    val Error = Color(0xFFBA1A1A)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)
    
    val Background = Color(0xFFFFFBFE)
    val OnBackground = Color(0xFF1C1B1F)
    val Surface = Color(0xFFFFFBFE)
    val OnSurface = Color(0xFF1C1B1F)
    val SurfaceVariant = Color(0xFFE7E0EC)
    val OnSurfaceVariant = Color(0xFF49454F)
    val Outline = Color(0xFF79747E)
}

/**
 * 暗色主题色彩定义
 */
object DarkBrandColors {
    val Primary = Color(0xFFD0BCFF)
    val OnPrimary = Color(0xFF371E73)
    val PrimaryContainer = Color(0xFF4F378B)
    val OnPrimaryContainer = Color(0xFFEDDDFF)
    
    val Secondary = Color(0xFFCCC2DC)
    val OnSecondary = Color(0xFF332D41)
    val SecondaryContainer = Color(0xFF4A4458)
    val OnSecondaryContainer = Color(0xFFE8DEF8)
    
    val Tertiary = Color(0xFFEFB8C8)
    val OnTertiary = Color(0xFF492532)
    val TertiaryContainer = Color(0xFF633B48)
    val OnTertiaryContainer = Color(0xFFFFD8E4)
    
    val Error = Color(0xFFFFB4AB)
    val OnError = Color(0xFF690005)
    val ErrorContainer = Color(0xFF93000A)
    val OnErrorContainer = Color(0xFFFFDAD6)
    
    val Background = Color(0xFF1C1B1F)
    val OnBackground = Color(0xFFE6E1E5)
    val Surface = Color(0xFF1C1B1F)
    val OnSurface = Color(0xFFE6E1E5)
    val SurfaceVariant = Color(0xFF49454F)
    val OnSurfaceVariant = Color(0xFFCAC4D0)
    val Outline = Color(0xFF938F99)
}

/**
 * 亮色主题配色方案
 */
val LightColorScheme = lightColorScheme(
    primary = BrandColors.Primary,
    onPrimary = BrandColors.OnPrimary,
    primaryContainer = BrandColors.PrimaryContainer,
    onPrimaryContainer = BrandColors.OnPrimaryContainer,
    secondary = BrandColors.Secondary,
    onSecondary = BrandColors.OnSecondary,
    secondaryContainer = BrandColors.SecondaryContainer,
    onSecondaryContainer = BrandColors.OnSecondaryContainer,
    tertiary = BrandColors.Tertiary,
    onTertiary = BrandColors.OnTertiary,
    tertiaryContainer = BrandColors.TertiaryContainer,
    onTertiaryContainer = BrandColors.OnTertiaryContainer,
    error = BrandColors.Error,
    onError = BrandColors.OnError,
    errorContainer = BrandColors.ErrorContainer,
    onErrorContainer = BrandColors.OnErrorContainer,
    background = BrandColors.Background,
    onBackground = BrandColors.OnBackground,
    surface = BrandColors.Surface,
    onSurface = BrandColors.OnSurface,
    surfaceVariant = BrandColors.SurfaceVariant,
    onSurfaceVariant = BrandColors.OnSurfaceVariant,
    outline = BrandColors.Outline
)

/**
 * 暗色主题配色方案
 */
val DarkColorScheme = darkColorScheme(
    primary = DarkBrandColors.Primary,
    onPrimary = DarkBrandColors.OnPrimary,
    primaryContainer = DarkBrandColors.PrimaryContainer,
    onPrimaryContainer = DarkBrandColors.OnPrimaryContainer,
    secondary = DarkBrandColors.Secondary,
    onSecondary = DarkBrandColors.OnSecondary,
    secondaryContainer = DarkBrandColors.SecondaryContainer,
    onSecondaryContainer = DarkBrandColors.OnSecondaryContainer,
    tertiary = DarkBrandColors.Tertiary,
    onTertiary = DarkBrandColors.OnTertiary,
    tertiaryContainer = DarkBrandColors.TertiaryContainer,
    onTertiaryContainer = DarkBrandColors.OnTertiaryContainer,
    error = DarkBrandColors.Error,
    onError = DarkBrandColors.OnError,
    errorContainer = DarkBrandColors.ErrorContainer,
    onErrorContainer = DarkBrandColors.OnErrorContainer,
    background = DarkBrandColors.Background,
    onBackground = DarkBrandColors.OnBackground,
    surface = DarkBrandColors.Surface,
    onSurface = DarkBrandColors.OnSurface,
    surfaceVariant = DarkBrandColors.SurfaceVariant,
    onSurfaceVariant = DarkBrandColors.OnSurfaceVariant,
    outline = DarkBrandColors.Outline
)
