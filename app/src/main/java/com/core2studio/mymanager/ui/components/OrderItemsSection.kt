package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.ui.screens.orders.dashedBorder

@Composable
fun OrderItemsSection(
    items: List<com.core2studio.mymanager.data.local.entity.OrderItemDraft>,
    onItemChange: (Int, com.core2studio.mymanager.data.local.entity.OrderItemDraft) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onAddItem: () -> Unit,
    modifier: Modifier = Modifier,
    productCatalog: List<Product> = emptyList(),
    currencySymbol: String
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEachIndexed { index, item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                OrderItemRow(
                    item = item,
                    index = index,
                    allProducts = productCatalog,
                    onQuantityChange = { qty -> onItemChange(index, item.copy(quantity = qty)) },
                    onPriceChange = { price -> onItemChange(index, item.copy(unitPrice = price)) },
                    onProductChange = { product -> onItemChange(index, item.copy(
                        productId = product?.id,
                        productName = product?.name ?: item.productName,
                        unitPrice = product?.price ?: item.unitPrice,
                        imageUrl = product?.imageUrls?.firstOrNull() ?: "",
                        hsnSacCode = product?.hsnSacCode ?: "",
                        hsnSacType = product?.hsnSacType ?: "HSN"
                    )) },
                    onProductNameChange = { name -> onItemChange(index, item.copy(productName = name)) },
                    onRemove = { onRemoveItem(index) },
                    currencySymbol = currencySymbol
                )
            }
        }

        HorizontalDivider()

        // Add Item Button - green dashed
        OutlinedButton(
            onClick = onAddItem,
            modifier = Modifier.fillMaxWidth().dashedBorder(
                color = MaterialTheme.colorScheme.primary,
                cornerRadius = 12.dp
            ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(0.dp, Color.Transparent)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("Add Item", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
