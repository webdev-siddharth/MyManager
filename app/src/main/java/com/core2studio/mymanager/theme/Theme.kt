package com.core2studio.mymanager.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MyManagerColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = White,
    secondary = SageGreen,
    onSecondary = White,
    tertiary = LimeAccent,
    onTertiary = White,
    background = MintCream,
    onBackground = DeepSlate,
    surface = PaleMint,
    onSurface = DeepSlate,
    surfaceVariant = PaleMint,
    onSurfaceVariant = DeepSlate,
    error = ErrorRed,
    onError = White,
    outline = SageGreen,
    outlineVariant = LightGray
)

private val MyManagerDarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = White,
    secondary = DarkSecondary,
    onSecondary = White,
    // TODO: consider a dedicated semantic "success" color token instead of reusing tertiary
    tertiary = DarkTertiary,
    onTertiary = White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    error = DarkError,
    onError = White,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant
)

private val MyManagerShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun MyManagerTheme(
    themeMode: Int = 0,
    content: @Composable () -> Unit
) {
    val isDark = if (themeMode == 0) isSystemInDarkTheme() else themeMode == 2
    val colorScheme = if (isDark) MyManagerDarkColorScheme else MyManagerColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MyManagerShapes,
        typography = Typography,
        content = content
    )
}
