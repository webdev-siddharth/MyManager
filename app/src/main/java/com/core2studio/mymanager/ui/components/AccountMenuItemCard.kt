package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class AccountMenuItemVariant {
    Default,
    Highlighted,
    Danger
}

@Composable
fun AccountMenuItemCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showVerifiedBadge: Boolean = false,
    variant: AccountMenuItemVariant = AccountMenuItemVariant.Default,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val accent = when (variant) {
        AccountMenuItemVariant.Danger -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    val borderColor = when (variant) {
        AccountMenuItemVariant.Highlighted -> MaterialTheme.colorScheme.primary
        AccountMenuItemVariant.Danger -> MaterialTheme.colorScheme.error
        AccountMenuItemVariant.Default -> MaterialTheme.colorScheme.outline
    }
    val containerColor = when (variant) {
        AccountMenuItemVariant.Danger -> MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }
    val circleSize = if (compact) 36.dp else 48.dp
    val iconSize = if (compact) 18.dp else 24.dp
    val gap = if (compact) 12.dp else 16.dp
    val cardPadding = if (compact) 12.dp else 16.dp

    MyManagerCard(
        modifier = modifier,
        onClick = onClick,
        borderColor = borderColor,
        containerColor = containerColor,
        contentPadding = cardPadding
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            Box(
                modifier = Modifier
                    .size(circleSize)
                    .background(color = accent.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(iconSize)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showVerifiedBadge) {
                    VerifiedBadge(modifier = Modifier.padding(top = 4.dp))
                }
            }

            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VerifiedBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = "Verified",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
