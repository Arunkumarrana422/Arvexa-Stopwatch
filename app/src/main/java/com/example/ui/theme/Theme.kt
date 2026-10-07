package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

private val RunStopDarkColorScheme = darkColorScheme(
    primary = BrandCyan,
    onPrimary = Color.Black,
    primaryContainer = BrandPurple,
    onPrimaryContainer = Color.White,
    secondary = BrandEmerald,
    onSecondary = Color.Black,
    secondaryContainer = CardSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = BrandPink,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextWhite,
    surface = SurfaceDark,
    onSurface = TextWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderDark
)

private val RunStopLightColorScheme = lightColorScheme(
    primary = BrandPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECE9FF),
    onPrimaryContainer = BrandPurple,
    secondary = BrandCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FF),
    onSecondaryContainer = Color(0xFF005273),
    tertiary = BrandPink,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextLightMode,
    surface = SurfaceLight,
    onSurface = TextLightMode,
    surfaceVariant = CardLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = CardBorderLight
)

val LocalIsDarkTheme = staticCompositionLocalOf { true }

object AppTheme {
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIsDarkTheme.current

    val textPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) TextWhite else TextLightMode

    val textSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) TextSecondary else TextSecondaryLight

    val cardBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) CardDark else CardLight

    val cardElevated: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF151E33) else Color(0xFFFFFFFF)

    val cardBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) CardBorderDark else CardBorderLight

    val ringTrack: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xFF141C2E) else Color(0xFFDDE5F2)

    val bottomNavBg: Color
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) Color(0xE60E1629) else Color(0xF2FFFFFF)

    val bottomNavBorder: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) {
            Brush.horizontalGradient(
                listOf(
                    Color(0x556C5CE7),
                    Color(0x3300C2FF),
                    Color(0x22FFFFFF),
                    Color(0x556C5CE7)
                )
            )
        } else {
            Brush.horizontalGradient(
                listOf(
                    Color(0x336C5CE7),
                    Color(0x3300C2FF),
                    Color(0x44CBD5E1),
                    Color(0x336C5CE7)
                )
            )
        }

    val backgroundGradient: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    BackgroundDark,
                    Color(0xFF091021),
                    Color(0xFF0C1428)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF3F6FB),
                    Color(0xFFE9EFF7),
                    Color(0xFFDFE7F3)
                )
            )
        }
}

@Composable
fun RunStopTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) RunStopDarkColorScheme else RunStopLightColorScheme

    androidx.compose.runtime.CompositionLocalProvider(
        LocalIsDarkTheme provides isDark
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
