package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.utils.CurrencyUtils
import coil3.compose.rememberAsyncImagePainter

@Composable
fun OrderItemRow(
    item: com.core2studio.mymanager.data.local.entity.OrderItemDraft,
    index: Int,
    allProducts: List<Product>,
    onQuantityChange: (Int) -> Unit,
    onPriceChange: (Double) -> Unit,
    onProductChange: (Product?) -> Unit,
    onProductNameChange: (String) -> Unit,
    onRemove: () -> Unit,
    currencySymbol: String
) {
    var productExpanded by remember { mutableStateOf(false) }
    var isCustomMode by remember(item.productId) { mutableStateOf(item.productId == null) }

    var rawPriceText by remember(item.productId) {
        mutableStateOf(if (item.unitPrice == 0.0) "" else String.format("%.2f", item.unitPrice))
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Product Selector
        Box {
            OutlinedTextField(
                value = item.productName,
                onValueChange = { newName ->
                    isCustomMode = true
                    onProductNameChange(newName)
                },
                label = { Text("Product Name") },
                placeholder = { Text("Type or Select Product") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Select product",
                        modifier = Modifier.size(24.dp).clickable { productExpanded = true }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            DropdownMenu(
                expanded = productExpanded,
                onDismissRequest = { productExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { Text("Custom Item", color = MaterialTheme.colorScheme.primary) },
                    onClick = {
                        isCustomMode = true
                        onProductChange(null)
                        productExpanded = false
                    }
                )
                if (allProducts.isNotEmpty()) {
                    HorizontalDivider()
                    allProducts.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (product.imageUrls.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(product.imageUrls.first()),
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                    Column {
                                        Text(product.name, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "$currencySymbol${CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(androidx.compose.ui.platform.LocalContext.current))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                isCustomMode = false
                                onProductChange(product)
                                rawPriceText = String.format("%.2f", product.price)
                                productExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Qty + Unit Price + Delete: labels above controls
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Labels row - weights match controls row for perfect alignment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Qty",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Unit Price ($currencySymbol)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Controls row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Qty stepper
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onQuantityChange(item.quantity - 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Decrease quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.quantity.toString(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onQuantityChange(item.quantity + 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Increase quantity",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Unit Price - custom boxed field
                Row(
                    modifier = Modifier
                        .weight(2f)
                        .height(52.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(36.dp)
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
                    VerticalDivider(
                        modifier = Modifier.fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outline
                    )
                    BasicTextField(
                        value = rawPriceText,
                        onValueChange = { newText ->
                            rawPriceText = newText
                            val parsed = newText.toDoubleOrNull()
                            if (parsed != null) onPriceChange(parsed)
                            else if (newText.isEmpty() || newText == ".") onPriceChange(0.0)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxHeight(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (rawPriceText.isEmpty()) {
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
                }

                // Delete
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "$currencySymbol${String.format("%.2f", item.subtotal)}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // HSN/SAC code display (if present)
        if (item.hsnSacCode.isNotBlank()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text("${item.hsnSacType}: ${item.hsnSacCode}", style = MaterialTheme.typography.labelSmall) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
