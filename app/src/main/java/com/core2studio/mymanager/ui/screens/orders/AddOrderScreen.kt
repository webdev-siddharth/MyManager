package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.screens.clients.AddClientDialog
import java.util.Locale

private val statusOptions = listOf("PENDING", "PARTIAL", "COMPLETED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrderScreen(
    onBack: () -> Unit,
    clients: List<Client> = emptyList(),
    onAddClient: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> },
    onSave: (
        clientId: String,
        productName: String,
        quantity: Int,
        unitPrice: Double,
        totalAmount: Double,
        paidAmount: Double,
        status: String,
        paymentMethod: String,
        notes: String,
        customFields: Map<String, String>
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currencySymbol = CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientMenuExpanded by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    var unitPrice by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash-in-hand") }
    var paymentMethodExpanded by remember { mutableStateOf(false) }

    val totalAmount = quantity * (unitPrice.toDoubleOrNull() ?: 0.0)
    var paidAmount by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("PENDING") }
    var notes by remember { mutableStateOf("") }

    // Custom fields
    val customFieldKeys = remember { mutableStateListOf<String>() }
    val customFieldValues = remember { mutableStateListOf<String>() }

    var clientError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedLabelColor = MaterialTheme.colorScheme.primary
    )

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = "New Order",
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Client selector
            ExposedDropdownMenuBox(
                expanded = clientMenuExpanded,
                onExpandedChange = { clientMenuExpanded = !clientMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedClient?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Client") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientMenuExpanded) },
                    isError = clientError,
                    supportingText = if (clientError) {
                        { Text("Please select a client") }
                    } else null,
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = clientMenuExpanded,
                    onDismissRequest = { clientMenuExpanded = false }
                ) {
                    clients.forEach { client ->
                        DropdownMenuItem(
                            text = { Text(client.name) },
                            onClick = {
                                selectedClient = client
                                clientError = false
                                clientMenuExpanded = false
                            }
                        )
                    }
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("+ Create New Client", color = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            clientMenuExpanded = false
                            showAddClientDialog = true
                        }
                    )
                    if (clients.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "No clients available",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = { clientMenuExpanded = false }
                        )
                    }
                }
            }

            // Add Client Dialog
            if (showAddClientDialog) {
                com.core2studio.mymanager.ui.screens.clients.AddClientDialog(
                    onDismiss = { showAddClientDialog = false },
                    onConfirm = { name, phone, email, address ->
                        onAddClient(name, phone, email, address)
                        showAddClientDialog = false
                    }
                )
            }



            // Product name (free text)
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            // Quantity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quantity",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { if (quantity > 1) quantity-- },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease quantity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { quantity++ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase quantity",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Unit Price
            OutlinedTextField(
                value = unitPrice,
                onValueChange = { unitPrice = it },
                label = { Text("Unit Price") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = textFieldColors
            )

            // Total Amount (auto-calculated)
            OutlinedTextField(
                value = if (totalAmount > 0) String.format(Locale.getDefault(), "%.2f", totalAmount) else "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Total Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text(currencySymbol) },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Enter unit price to calculate total") }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.outline
                )
            )

            // Status chips
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.core2studio.mymanager.ui.screens.orders.statusOptions.forEach { status ->
                    val chipColor = when (status) {
                        "PENDING" -> Color(0xFFD32F2F)
                        "PARTIAL" -> Color(0xFFF57C00)
                        "COMPLETED" -> Color(0xFF388E3C)
                        else -> MaterialTheme.colorScheme.primary
                    }
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = status
                            when (status) {
                                "PENDING" -> paidAmount = "0"
                                "COMPLETED" -> {
                                    paidAmount = if (totalAmount > 0) totalAmount.toBigDecimal().toPlainString() else "0"
                                }
                            }
                        },
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

            // Paid Amount
            val isPaidEditable = selectedStatus == "PARTIAL"
            OutlinedTextField(
                value = paidAmount,
                onValueChange = { paidAmount = it },
                enabled = isPaidEditable,
                label = { Text("Advance Paid Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text(currencySymbol) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.outline
                )
            )

            // Payment Method
            ExposedDropdownMenuBox(
                expanded = paymentMethodExpanded,
                onExpandedChange = { paymentMethodExpanded = !paymentMethodExpanded }
            ) {
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Method") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMethodExpanded) },
                    colors = textFieldColors
                )
                ExposedDropdownMenu(
                    expanded = paymentMethodExpanded,
                    onDismissRequest = { paymentMethodExpanded = false }
                ) {
                    listOf("Cash-in-hand", "UPI", "Bank Transfer").forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method) },
                            onClick = {
                                paymentMethod = method
                                paymentMethodExpanded = false
                            }
                        )
                    }
                }
            }


            // Custom Fields
            Text(
                text = "Custom Fields",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            customFieldKeys.forEachIndexed { index, key ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = key,
                        onValueChange = { customFieldKeys[index] = it },
                        label = { Text("Key") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = customFieldValues[index],
                        onValueChange = { customFieldValues[index] = it },
                        label = { Text("Value") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = textFieldColors
                    )
                    IconButton(
                        onClick = {
                            customFieldKeys.removeAt(index)
                            customFieldValues.removeAt(index)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove field",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    customFieldKeys.add("")
                    customFieldValues.add("")
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Custom Field")
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val isClientValid = selectedClient != null
                    val unitPriceValue = unitPrice.toDoubleOrNull() ?: 0.0
                    val isAmountValid = totalAmount > 0

                    clientError = !isClientValid
                    amountError = !isAmountValid

                    if (isClientValid && isAmountValid) {
                        val customFieldsMap = mutableMapOf<String, String>()
                        customFieldKeys.forEachIndexed { index, k ->
                            if (k.isNotBlank()) {
                                customFieldsMap[k] = customFieldValues[index]
                            }
                        }

                        onSave(
                            selectedClient!!.id,
                            productName.trim(),
                            quantity,
                            unitPriceValue,
                            totalAmount,
                            paidAmount.toDoubleOrNull() ?: 0.0,
                            selectedStatus,
                            paymentMethod,
                            notes.trim(),
                            customFieldsMap
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    text = "Save Order",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
