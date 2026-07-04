package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.LimeAccent
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White

@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (status.uppercase()) {
        "COMPLETED" -> LimeAccent
        "PARTIAL" -> SageGreen
        else -> White // PENDING
    }

    val textColor = when (status.uppercase()) {
        "COMPLETED" -> White
        "PARTIAL" -> White
        else -> DeepSlate // PENDING
    }

    val border = when (status.uppercase()) {
        "PENDING" -> BorderStroke(1.dp, DeepSlate)
        else -> null
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = border
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
