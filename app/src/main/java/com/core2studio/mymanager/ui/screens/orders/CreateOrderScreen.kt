package com.core2studio.mymanager.ui.screens.orders

import android.app.AlertDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.ui.components.ClientPickerField
import com.core2studio.mymanager.ui.components.ButtonLoadingIndicator
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.OrderItemsSection
import com.core2studio.mymanager.ui.components.PaymentSection
import com.core2studio.mymanager.ui.components.PricingSummaryCard
import com.core2studio.mymanager.ui.components.SectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    onBack: () -> Unit,
    draftId: String? = null,
    preSelectedClientId: String? = null,
    clients: List<Client> = emptyList(),
    products: List<Product> = emptyList(),
    viewModel: CreateOrderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(LocalContext.current))
    val context = LocalContext.current

    var showDiscardDialog by remember { mutableStateOf(false) }
    var showAddClientDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Load draft if draftId provided
    LaunchedEffect(draftId) {
        draftId?.let { viewModel.loadDraft(it) }
    }

    // Pre-select client if provided
    LaunchedEffect(preSelectedClientId) {
        preSelectedClientId?.let { id ->
            clients.find { it.id == id }?.let { viewModel.setClient(it) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MyManagerTopBar(
                title = if (draftId != null) "Edit Order" else "New Order",
                onBackClick = {
                    if (uiState.isDraftDirty) {
                        showDiscardDialog = true
                    } else {
                        onBack()
                    }
                }
            )
        },
        bottomBar = {
            BottomActionBar(
                viewModel = viewModel,
                uiState = uiState,
                onBack = onBack,
                currencySymbol = currencySymbol
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Client
            SectionCard(
                title = "Client",
                icon = Icons.Filled.People,
                subtitle = "Select a client for this order"
            ) {
                ClientPickerField(
                    clients = clients,
                    selectedClient = clients.find { it.id == uiState.clientId },
                    onSelect = viewModel::setClient,
                    onClear = { viewModel.clearClient() },
                    onCreateNew = { showAddClientDialog = true }
                )
            }

            // Section 2: Order Items
            SectionCard(
                title = "Order Items",
                icon = Icons.Filled.ShoppingBag,
                subtitle = "Add products or services to this order"
            ) {
                OrderItemsSection(
                    items = uiState.items,
                    onItemChange = viewModel::updateItem,
                    onRemoveItem = viewModel::removeItem,
                    onAddItem = { viewModel.addItem(null) },
                    productCatalog = products,
                    currencySymbol = currencySymbol
                )
            }

            // Section 3: Pricing Summary
            SectionCard(
                title = "Pricing",
                icon = Icons.Filled.Calculate,
                subtitle = "Order summary and tax calculation"
            ) {
                PricingSummaryCard(
                    subtotal = uiState.subtotal,
                    discountAmount = uiState.discountAmount,
                    taxableAmount = uiState.taxableAmount,
                    totalCgst = uiState.totalCgst,
                    totalSgst = uiState.totalSgst,
                    totalIgst = uiState.totalIgst,
                    totalTax = uiState.totalTax,
                    grandTotal = uiState.grandTotal,
                    roundedGrandTotal = uiState.roundedGrandTotal,
                    roundOffAmount = uiState.roundOffAmount,
                    hasGstin = uiState.hasGstin,
                    gstType = uiState.gstType,
                    gstRate = uiState.gstRate,
                    currencySymbol = currencySymbol,
                    discountType = uiState.discountType,
                    discountValue = uiState.discountValue,
                    onDiscountChange = viewModel::setDiscount
                )
            }

            // Section 4: Payment
            SectionCard(
                title = "Payment",
                icon = Icons.Filled.Wallet,
                subtitle = "Record payment details for this order"
            ) {
                PaymentSection(
                    status = uiState.status,
                    onStatusChange = viewModel::setStatus,
                    paidAmount = uiState.paidAmount,
                    onPaidAmountChange = viewModel::setPaidAmount,
                    paymentMethod = uiState.paymentMethod,
                    onPaymentMethodChange = viewModel::setPaymentMethod,
                    referenceNumber = uiState.referenceNumber,
                    onReferenceChange = viewModel::setReferenceNumber,
                    grandTotal = uiState.grandTotal,
                    currencySymbol = currencySymbol
                )
            }

            // Section 5: Additional (collapsible)
            var additionalExpanded by remember { mutableStateOf(false) }
            SectionCard(
                title = "Additional Details",
                subtitle = "Add any extra information for this order",
                isCollapsible = true,
                isExpanded = additionalExpanded,
                onToggle = { additionalExpanded = !additionalExpanded }
            ) {
                // Notes
                Text("Notes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Add a note for this order (optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::setNotes,
                    placeholder = { Text("Write a note...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    supportingText = { Text("${uiState.notes.length}/300") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // Custom Fields
                Text("Custom Fields", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text("Add custom information fields (optional)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                val entriesList = uiState.customFields.entries.toList()
                entriesList.forEachIndexed { index, entry ->
                    key(index) {
                        val k = entry.key
                        val v = entry.value
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = k,
                                    onValueChange = { newKey ->
                                        val newMap = linkedMapOf<String, String>()
                                        uiState.customFields.entries.forEachIndexed { i, (key, value) ->
                                            if (i == index) newMap[newKey] = value
                                            else newMap[key] = value
                                        }
                                        viewModel.setCustomFields(newMap)
                                    },
                                    placeholder = { Text("Field Label") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                OutlinedTextField(
                                    value = v,
                                    onValueChange = { newValue ->
                                        viewModel.setCustomFields(uiState.customFields + (k to newValue))
                                    },
                                    placeholder = { Text("Value") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                IconButton(onClick = {
                                    viewModel.setCustomFields(uiState.customFields - k)
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Add Custom Field button - green dashed border
                OutlinedButton(
                    onClick = {
                        viewModel.setCustomFields(uiState.customFields + ("" to ""))
                    },
                    modifier = Modifier.fillMaxWidth().dashedBorder(
                        color = MaterialTheme.colorScheme.primary,
                        cornerRadius = 12.dp
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(0.dp, Color.Transparent)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("Add Custom Field", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Add Client Dialog
    if (showAddClientDialog) {
        AddClientDialog(
            onDismiss = { showAddClientDialog = false },
            onConfirm = { name, phone, email, address ->
                viewModel.addClientFromScreen(name, phone, email, address)
                showAddClientDialog = false
            }
        )
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.discardDraft()
                    showDiscardDialog = false
                    onBack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun BottomActionBar(
    viewModel: CreateOrderViewModel,
    uiState: CreateOrderUiState,
    onBack: () -> Unit,
    currencySymbol: String
) {
    androidx.compose.material3.BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.padding(bottom = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Payable amount
            Column {
                Text("Payable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$currencySymbol${uiState.roundedGrandTotal}", style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (uiState.isValid) {
                            viewModel.submitOrder()
                        }
                    },
                    enabled = uiState.isValid && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isLoading) {
                        ButtonLoadingIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Place Order", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun AddClientDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Client") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Name is required") } } else null
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name.trim(), phone.trim(), email.trim(), address.trim())
                } else {
                    nameError = true
                }
            }) {
                Text("Add Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Dashed border modifier
fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 1.dp,
    dashLength: Float = 10f,
    gapLength: Float = 10f
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength))
    )
    drawRoundRect(
        color = color,
        topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2),
        size = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx()),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = stroke
    )
}
