package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StatusRed = Color(0xFFD32F2F)
private val StatusOrange = Color(0xFFF57C00)
private val StatusGreen = Color(0xFF388E3C)

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status.uppercase()) {
        "COMPLETED" -> StatusGreen
        "PARTIAL" -> StatusOrange
        else -> StatusRed
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = status.uppercase(),
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
