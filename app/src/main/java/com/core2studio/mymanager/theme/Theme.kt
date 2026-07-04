package com.core2studio.mymanager.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val MyManagerColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = White,
    secondary = SageGreen,
    onSecondary = White,
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

private val MyManagerShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun MyManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MyManagerColorScheme,
        shapes = MyManagerShapes,
        typography = Typography,
        content = content
    )
}
