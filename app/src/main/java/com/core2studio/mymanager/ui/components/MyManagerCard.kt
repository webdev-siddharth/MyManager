package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.SageGreen

@Composable
fun MyManagerCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier.fillMaxWidth()

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = PaleMint
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            border = BorderStroke(1.dp, SageGreen)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = PaleMint
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            ),
            border = BorderStroke(1.dp, SageGreen)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}
