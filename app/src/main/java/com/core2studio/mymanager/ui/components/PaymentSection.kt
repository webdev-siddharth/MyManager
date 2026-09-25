package com.core2studio.mymanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PaymentSection(
    status: String,
    onStatusChange: (String) -> Unit,
    paidAmount: Double,
    onPaidAmountChange: (Double) -> Unit,
    paymentMethod: String,
    onPaymentMethodChange: (String) -> Unit,
    referenceNumber: String,
    onReferenceChange: (String) -> Unit,
    grandTotal: Double,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    val isPaidEditable = status == "PARTIAL"

    var rawPaidText by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Payment Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Payment Status", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                Text("Set the current payment status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf(
                        Pair("PENDING", com.core2studio.mymanager.theme.StatusPending),
                        Pair("PARTIAL", com.core2studio.mymanager.theme.StatusPartial),
                        Pair("COMPLETED", com.core2studio.mymanager.theme.StatusCompleted)
                    )
                    statusOptions.forEach { (key, color) ->
                        StatusCard(
                            label = key,
                            isSelected = status == key,
                            selectedColor = color,
                            onClick = { onStatusChange(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Paid Amount
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Paid Amount", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                Text("Enter the amount received from client", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                // Custom boxed field with currency prefix cell
                val borderColor = if (isPaidEditable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            currencySymbol,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                        BasicTextField(
                        value = rawPaidText,
                        onValueChange = { newText ->
                            if (isPaidEditable) {
                                val parsed = newText.toDoubleOrNull()
                                when {
                                    newText.isEmpty() || newText == "." -> {
                                        rawPaidText = newText
                                        onPaidAmountChange(0.0)
                                    }
                                    parsed != null && (grandTotal <= 0.0 || parsed < grandTotal) -> {
                                        rawPaidText = newText
                                        onPaidAmountChange(parsed)
                                    }
                                    // Reject input that would reach or exceed the payable amount
                                }
                            }
                        },
                        enabled = isPaidEditable,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isPaidEditable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (rawPaidText.isEmpty()) {
                                    Text(
                                        "0.00",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (!isPaidEditable) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Amount not editable",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(18.dp)
                        )
                    }
                }
            }
        }

        // Payment Method
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Payment Method", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                Text("Select how the client is paying", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val methods = listOf(
                        Triple("Cash", Icons.Filled.AttachMoney, "Cash"),
                        Triple("UPI", Icons.Filled.AccountBalance, "UPI"),
                        Triple("Card", Icons.Filled.CreditCard, "Card"),
                        Triple("Bank Transfer", Icons.Filled.Business, "Bank"),
                        Triple("Other", Icons.Filled.MoreHoriz, "Other")
                    )
                    methods.forEach { (key, icon, shortLabel) ->
                        MethodCard(
                            label = shortLabel,
                            icon = icon,
                            isSelected = paymentMethod == key,
                            onClick = { onPaymentMethodChange(key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Transaction Reference (conditional for non-Cash)
        AnimatedVisibility(visible = paymentMethod != "Cash") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Transaction Reference (Optional)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                    Text("UTR, Transaction ID, or reference number", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = referenceNumber,
                        onValueChange = onReferenceChange,
                        placeholder = { Text("Enter reference number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    }

    // Status-driven paid-amount display + validation
    DisposableEffect(status, grandTotal) {
        when (status) {
            "PENDING" -> {
                rawPaidText = "0.00"
                if (paidAmount != 0.0) onPaidAmountChange(0.0)
            }
            "COMPLETED" -> {
                rawPaidText = if (grandTotal > 0) String.format("%.2f", grandTotal) else "0.00"
                if (paidAmount != grandTotal) onPaidAmountChange(grandTotal)
            }
            "PARTIAL" -> {
                if (grandTotal > 0.0 && paidAmount >= grandTotal) {
                    val clamped = (grandTotal - 0.01).coerceAtLeast(0.0)
                    rawPaidText = String.format("%.2f", clamped)
                    onPaidAmountChange(clamped)
                } else {
                    rawPaidText = if (paidAmount > 0.0) String.format("%.2f", paidAmount) else ""
                }
            }
        }
        onDispose {}
    }
}

@Composable
private fun StatusCard(
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) selectedColor else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MethodCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
    val iconTint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(24.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
