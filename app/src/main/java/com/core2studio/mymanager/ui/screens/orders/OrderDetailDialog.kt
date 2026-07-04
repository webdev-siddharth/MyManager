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
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.White
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
    var selectedStatus by remember { mutableStateOf(order.status) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MintCream,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        title = {
            Text(
                text = "Order Details",
                style = MaterialTheme.typography.titleLarge,
                color = DeepSlate
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = clientName,
                    style = MaterialTheme.typography.titleMedium,
                    color = ForestGreen
                )
                if (order.productName.isNotBlank()) {
                    Text(
                        text = "Product: ${order.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate
                    )
                }
                Text(
                    text = "Amount: ₹${String.format(Locale.getDefault(), "%.2f", order.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepSlate
                )
                Text(
                    text = "Paid: ₹${String.format(Locale.getDefault(), "%.2f", order.paidAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepSlate
                )
                Text(
                    text = "Date: ${dateFormatter.format(Date(order.date))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepSlate
                )
                if (order.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${order.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Update Status",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepSlate
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.core2studio.mymanager.ui.screens.orders.statusOptions.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ForestGreen,
                                selectedLabelColor = White,
                                containerColor = PaleMint,
                                labelColor = DeepSlate
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = ForestGreen,
                                selectedBorderColor = ForestGreen,
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
                    containerColor = ForestGreen,
                    contentColor = White
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
                    contentColor = ForestGreen
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
