package com.core2studio.mymanager.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PricingSummaryCard(
    subtotal: Double,
    discountAmount: Double,
    taxableAmount: Double,
    totalCgst: Double,
    totalSgst: Double,
    totalIgst: Double,
    totalTax: Double,
    grandTotal: Double,
    roundedGrandTotal: Long,
    roundOffAmount: Double,
    hasGstin: Boolean,
    gstType: String,
    gstRate: Int,
    currencySymbol: String,
    discountType: String,
    discountValue: Double,
    onDiscountChange: (String, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var taxExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Subtotal
        PricingDetailRow(
            label = "Subtotal",
            subtitle = "Total amount before discount",
            value = "$currencySymbol${String.format("%.2f", subtotal)}"
        )

        // Discount
        DiscountRow(
            currencySymbol = currencySymbol,
            discountValue = discountValue,
            discountAmount = discountAmount,
            onDiscountChange = onDiscountChange
        )

        // Taxable Amount (GST only)
        if (hasGstin) {
            PricingDetailRow(
                label = "Taxable Amount",
                subtitle = if (totalTax > 0) "Before tax, after discount" else "After discount",
                value = "$currencySymbol${String.format("%.2f", taxableAmount)}"
            )
        }

        // Tax (collapsible)
        if (hasGstin && totalTax > 0) {
            TaxBreakdownCard(
                gstType = gstType,
                gstRate = gstRate,
                totalCgst = totalCgst,
                totalSgst = totalSgst,
                totalIgst = totalIgst,
                totalTax = totalTax,
                currencySymbol = currencySymbol,
                isExpanded = taxExpanded,
                onToggle = { taxExpanded = !taxExpanded }
            )
        }

        // Grand Total
        HighlightCard(
            label = "Grand Total",
            subtitle = "",
            value = "$currencySymbol${String.format("%.2f", grandTotal)}"
        )

        // Round Off
        if (roundOffAmount != 0.0) {
            RoundOffRow(
                currencySymbol = currencySymbol,
                roundOffAmount = roundOffAmount
            )
        }

        // Payable Amount
        HighlightCard(
            label = "Payable Amount",
            subtitle = "Amount to be paid by client",
            value = "$currencySymbol$roundedGrandTotal"
        )
    }
}

@Composable
private fun PricingDetailRow(
    label: String,
    subtitle: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
        }
    }
}

@Composable
private fun DiscountRow(
    currencySymbol: String,
    discountValue: Double,
    discountAmount: Double,
    onDiscountChange: (String, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Discount", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                    Text("Apply discount to this order", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Stepper pill
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onDiscountChange("PERCENT", (discountValue - 1.0).coerceAtLeast(0.0)) },
                        enabled = discountValue > 0,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "${discountValue.toInt()}%",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { onDiscountChange("PERCENT", (discountValue + 1.0).coerceAtMost(100.0)) },
                        enabled = discountValue < 100,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (discountAmount > 0) {
                Text(
                    "- $currencySymbol${String.format("%.2f", discountAmount)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun TaxBreakdownCard(
    gstType: String,
    gstRate: Int,
    totalCgst: Double,
    totalSgst: Double,
    totalIgst: Double,
    totalTax: Double,
    currencySymbol: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { onToggle() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Text("Tax (${gstRate}%)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = MaterialTheme.colorScheme.primary)
                        Text("Taxes calculated on taxable amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).padding(4.dp)
                )
            }

            // Tax rows
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    HorizontalDivider()
                    if (gstType == "CGST_SGST") {
                        val halfRate = String.format("%.1f", gstRate / 2.0).removeSuffix(".0")
                        TaxRow("CGST ($halfRate%)", "$currencySymbol${String.format("%.2f", totalCgst)}")
                        TaxRow("SGST ($halfRate%)", "$currencySymbol${String.format("%.2f", totalSgst)}")
                    } else {
                        TaxRow("IGST ($gstRate%)", "$currencySymbol${String.format("%.2f", totalIgst)}")
                    }
                    HorizontalDivider()
                    TaxRow("Total Tax", "$currencySymbol${String.format("%.2f", totalTax)}", isBold = true)
                }
            }
        }
    }
}

@Composable
private fun TaxRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = if (isBold) MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HighlightCard(
    label: String,
    subtitle: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
                if (subtitle.isNotBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RoundOffRow(
    currencySymbol: String,
    roundOffAmount: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Round Off", style = MaterialTheme.typography.titleSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold))
            Text("Adjustment for nearest rupee", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            "${if (roundOffAmount > 0) "+" else ""}$currencySymbol${String.format("%.2f", roundOffAmount)}",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
        )
    }
}
