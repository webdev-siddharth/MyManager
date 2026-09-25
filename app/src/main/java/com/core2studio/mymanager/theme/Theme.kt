package com.core2studio.mymanager.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val MyManagerColorScheme = lightColorScheme(
    primary = HeartwoodPrimary,
    onPrimary = White,
    primaryContainer = Color(0xFFF2E0C4),
    onPrimaryContainer = HeartwoodTextPrimary,
    secondary = HeartwoodSecondary,
    onSecondary = White,
    secondaryContainer = Color(0xFFDCE7DE),
    onSecondaryContainer = Color(0xFF1F3427),
    tertiary = Color(0xFFC97A2E),
    onTertiary = White,
    background = HeartwoodBackground,
    onBackground = HeartwoodTextPrimary,
    surface = HeartwoodSurface,
    onSurface = HeartwoodTextPrimary,
    surfaceVariant = HeartwoodSurface,
    onSurfaceVariant = HeartwoodTextMuted,
    surfaceContainerLowest = Color(0xFFFFFDF8),
    surfaceContainerLow = Color(0xFFF5EFE1),
    surfaceContainer = Color(0xFFF5EFE1),
    surfaceContainerHigh = Color(0xFFF1EADB),
    surfaceContainerHighest = Color(0xFFEDE4D2),
    error = ErrorRed,
    onError = White,
    outline = HeartwoodBorder,
    outlineVariant = HeartwoodTextMuted,
    inverseSurface = HeartwoodTextPrimary,
    inverseOnSurface = HeartwoodDarkTextPrimary,
    inversePrimary = Color(0xFFF0D4A8)
)

private val MyManagerDarkColorScheme = darkColorScheme(
    primary = HeartwoodDarkPrimary,
    onPrimary = HeartwoodDarkBackground,
    primaryContainer = Color(0xFF4A3018),
    onPrimaryContainer = Color(0xFFF0D4A8),
    secondary = HeartwoodDarkSecondary,
    onSecondary = HeartwoodDarkBackground,
    secondaryContainer = Color(0xFF37473D),
    onSecondaryContainer = Color(0xFFC8DCCB),
    tertiary = HeartwoodDarkPrimary,
    onTertiary = HeartwoodDarkBackground,
    background = HeartwoodDarkBackground,
    onBackground = HeartwoodDarkTextPrimary,
    surface = HeartwoodDarkSurface,
    onSurface = HeartwoodDarkTextPrimary,
    surfaceVariant = HeartwoodDarkSurface,
    onSurfaceVariant = HeartwoodDarkTextMuted,
    surfaceContainerLowest = Color(0xFF1B120C),
    surfaceContainerLow = Color(0xFF1D140E),
    surfaceContainer = HeartwoodDarkSurface,
    surfaceContainerHigh = Color(0xFF271C15),
    surfaceContainerHighest = Color(0xFF2E211A),
    error = DarkError,
    onError = White,
    outline = HeartwoodDarkBorder,
    outlineVariant = HeartwoodDarkTextMuted,
    inverseSurface = HeartwoodDarkTextPrimary,
    inverseOnSurface = HeartwoodTextPrimary,
    inversePrimary = HeartwoodPrimary
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
