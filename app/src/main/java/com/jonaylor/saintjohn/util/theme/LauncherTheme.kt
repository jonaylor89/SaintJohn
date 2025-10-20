package com.jonaylor.saintjohn.util.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Blloc Mode - Dark Monochrome
val BllocDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE0E0E0),
    onPrimary = Color(0xFF121212),
    primaryContainer = Color(0xFF2C2C2C),
    onPrimaryContainer = Color(0xFFE0E0E0),

    secondary = Color(0xFFBDBDBD),
    onSecondary = Color(0xFF1E1E1E),
    secondaryContainer = Color(0xFF2C2C2C),
    onSecondaryContainer = Color(0xFFBDBDBD),

    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color(0xFF1E1E1E),
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = Color(0xFF9E9E9E),

    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),

    error = Color(0xFFB0B0B0),
    onError = Color(0xFF1E1E1E),

    outline = Color(0xFF424242),
    outlineVariant = Color(0xFF303030)
)

// Sun Mode - Light Monochrome
val SunLightColorScheme = lightColorScheme(
    primary = Color(0xFF1C1C1C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF1C1C1C),

    secondary = Color(0xFF424242),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E0E0),
    onSecondaryContainer = Color(0xFF424242),

    tertiary = Color(0xFF616161),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0E0E0),
    onTertiaryContainer = Color(0xFF616161),

    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1C1C),

    surface = Color(0xFFF5F5F5),
    onSurface = Color(0xFF1C1C1C),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF424242),

    error = Color(0xFF4F4F4F),
    onError = Color(0xFFFFFFFF),

    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0)
)

@Composable
fun LauncherTheme(
    themeMode: String = "BLLOC", // "BLLOC", "SUN", or "COLOR"
    content: @Composable () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()

    val colorScheme: ColorScheme = when (themeMode) {
        "BLLOC" -> BllocDarkColorScheme
        "SUN" -> SunLightColorScheme
        "COLOR" -> if (isDarkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
        else -> BllocDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LauncherTypography,
        content = content
    )
}
