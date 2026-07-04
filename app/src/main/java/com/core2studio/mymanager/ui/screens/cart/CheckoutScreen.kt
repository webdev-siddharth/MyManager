package com.core2studio.mymanager.ui.screens.cart

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import java.util.Locale

private val statusOptions = listOf("PENDING", "PARTIAL", "COMPLETED")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    clients: List<Client> = emptyList(),
    onBack: () -> Unit,
    onAddClient: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by cartViewModel.uiState.collectAsState()

    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientMenuExpanded by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("PENDING") }
    var paidAmount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var clientError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val customFieldKeys = remember { mutableStateListOf<String>() }
    val customFieldValues = remember { mutableStateListOf<String>() }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            showSuccessDialog = true
            cartViewModel.resetCheckoutSuccess()
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Order Placed!") },
            text = { Text("Your order has been created successfully and will appear on the Orders page.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    }
                ) {
                    Text("OK", color = ForestGreen)
                }
            }
        )
    }

    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onConfirm = { name, phone, email, address ->
                onAddClient(name, phone, email, address)
                showAddClientDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = "Checkout",
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
                    label = { Text("Select Client *") },
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

            // Order summary
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepSlate
            )

            uiState.cartItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.productName} x${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.2f", item.price * item.quantity)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate
                    )
                }
            }

            HorizontalDivider(color = ForestGreen.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepSlate
                )
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%.2f", uiState.cartTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
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
                statusOptions.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = status
                            when (status) {
                                "PENDING" -> paidAmount = "0"
                                "COMPLETED" -> {
                                    paidAmount = String.format(Locale.getDefault(), "%.2f", uiState.cartTotal)
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

            // Place Order button
            Button(
                onClick = {
                    val isClientValid = selectedClient != null
                    clientError = !isClientValid

                    if (isClientValid) {
                        val customFieldsMap = mutableMapOf<String, String>()
                        customFieldKeys.forEachIndexed { index, k ->
                            if (k.isNotBlank()) {
                                customFieldsMap[k] = customFieldValues[index]
                            }
                        }

                        cartViewModel.checkout(
                            clientId = selectedClient!!.id,
                            notes = notes.trim(),
                            status = selectedStatus,
                            paidAmount = paidAmount.toDoubleOrNull() ?: 0.0,
                            customFields = customFieldsMap
                        )
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
                    text = "Place Order",
                    style = MaterialTheme.typography.bodyLarge,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
