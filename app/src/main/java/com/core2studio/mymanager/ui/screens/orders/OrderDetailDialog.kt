package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val statusOptions = listOf("PENDING", "PARTIAL", "COMPLETED")

@Composable
fun OrderDetailDialog(
    order: Order,
    clientName: String,
    onDismiss: () -> Unit,
    onStatusUpdate: (Order) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedStatus by remember { mutableStateOf(order.status) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "Order Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (order.productName.isNotBlank()) {
                    Text(
                        text = "Product: ${order.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (order.quantity > 1 || order.unitPrice > 0) {
                    Text(
                        text = "Quantity: ${order.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Unit Price: ${CurrencyUtils.formatCurrency(order.unitPrice, CurrencyUtils.loadCurrencyCode(context))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (order.paymentMethod.isNotBlank()) {
                    Text(
                        text = "Payment: ${order.paymentMethod}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Amount: ${CurrencyUtils.formatCurrency(order.amount, CurrencyUtils.loadCurrencyCode(context))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Paid: ${CurrencyUtils.formatCurrency(order.paidAmount, CurrencyUtils.loadCurrencyCode(context))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Date: ${dateFormatter.format(Date(order.date))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (order.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${order.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Update Status",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statusOptions.forEach { status ->
                        val chipColor = when (status) {
                            "PENDING" -> com.core2studio.mymanager.theme.StatusPending
                            "PARTIAL" -> com.core2studio.mymanager.theme.StatusPartial
                            "COMPLETED" -> com.core2studio.mymanager.theme.StatusCompleted
                            else -> MaterialTheme.colorScheme.primary
                        }
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipColor,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = chipColor,
                                selectedBorderColor = chipColor,
                                enabled = true,
                                selected = selectedStatus == status
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedStatus != order.status) {
                        val updatedOrder = when (selectedStatus) {
                            "COMPLETED" -> order.copy(status = selectedStatus, paidAmount = order.amount)
                            else -> order.copy(status = selectedStatus)
                        }
                        onStatusUpdate(updatedOrder)
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
