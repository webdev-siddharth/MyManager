package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ErrorRed
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.screens.clients.AddClientDialog

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
        totalAmount: Double,
        paidAmount: Double,
        status: String,
        notes: String,
        customFields: Map<String, String>
    ) -> Unit = { _, _, _, _, _, _, _ -> }
) {
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientMenuExpanded by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }

    var productName by remember { mutableStateOf("") }

    var totalAmount by remember { mutableStateOf("") }
    var paidAmount by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("PENDING") }
    var notes by remember { mutableStateOf("") }

    // Custom fields
    val customFieldKeys = remember { mutableStateListOf<String>() }
    val customFieldValues = remember { mutableStateListOf<String>() }

    var clientError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = "New Order",
                onBackClick = onBack
            )
        },
        containerColor = MintCream
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
                        text = { Text("+ Create New Client", color = ForestGreen) },
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
                                    color = DeepSlate.copy(alpha = 0.5f)
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

            // Status chips
            Text(
                text = "Status",
                style = MaterialTheme.typography.bodyLarge,
                color = DeepSlate
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                com.core2studio.mymanager.ui.screens.orders.statusOptions.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = status
                            when (status) {
                                "PENDING" -> paidAmount = "0"
                                "COMPLETED" -> {
                                    val total = totalAmount.toDoubleOrNull()
                                    paidAmount = if (total != null && total > 0) totalAmount else "0"
                                }
                            }
                        },
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

            // Product name (free text)
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = textFieldColors
            )

            // Amount
            OutlinedTextField(
                value = totalAmount,
                onValueChange = {
                    totalAmount = it
                    amountError = false
                    if (selectedStatus == "COMPLETED") {
                        val total = it.toDoubleOrNull()
                        paidAmount = if (total != null && total > 0) it else "0"
                    }
                },
                label = { Text("Total Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Valid amount is required") }
                } else null,
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = textFieldColors
            )

            // Paid Amount
            val isPaidEditable = selectedStatus == "PARTIAL"
            OutlinedTextField(
                value = paidAmount,
                onValueChange = { paidAmount = it },
                enabled = isPaidEditable,
                label = { Text("Advance Paid Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ForestGreen,
                    unfocusedBorderColor = SageGreen,
                    cursorColor = ForestGreen,
                    focusedLabelColor = ForestGreen,
                    disabledTextColor = DeepSlate,
                    disabledBorderColor = SageGreen,
                    disabledLabelColor = SageGreen
                )
            )

            // Custom Fields
            Text(
                text = "Custom Fields",
                style = MaterialTheme.typography.bodyLarge,
                color = DeepSlate
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
                            tint = ErrorRed
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
                    contentColor = ForestGreen
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
                    val amountValue = totalAmount.toDoubleOrNull()
                    val isAmountValid = amountValue != null && amountValue > 0

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
                            amountValue!!,
                            paidAmount.toDoubleOrNull() ?: 0.0,
                            selectedStatus,
                            notes.trim(),
                            customFieldsMap
                        )
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = White
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
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
