package com.core2studio.mymanager.ui.components

import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.White

@Composable
fun MyManagerFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    text: String? = null
) {
    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = ForestGreen,
            contentColor = White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            icon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = White
                )
            },
            text = {
                Text(
                    text = text,
                    color = White
                )
            }
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = ForestGreen,
            contentColor = White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = White
            )
        }
    }
}
