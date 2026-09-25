package com.core2studio.mymanager.ui.screens.cart

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.OrderItemDraft
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.ui.components.ButtonLoadingIndicator
import com.core2studio.mymanager.ui.components.ClientPickerField
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.PaymentSection
import com.core2studio.mymanager.ui.components.PricingSummaryCard
import com.core2studio.mymanager.ui.components.SectionCard
import com.core2studio.mymanager.ui.screens.clients.AddClientDialog
import com.core2studio.mymanager.ui.screens.orders.dashedBorder
import com.core2studio.mymanager.utils.TaxCalculator

@Composable
fun CheckoutScreen(
    cartViewModel: CartViewModel,
    clients: List<Client> = emptyList(),
    onBack: () -> Unit,
    onAddClient: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> },
    gstEnabled: Boolean = false,
    gstin: String = "",
    gstPricingMode: String = "INCLUSIVE",
    gstRate: Int = 18,
    gstType: String = "CGST_SGST"
) {
    val context = LocalContext.current
    val uiState by cartViewModel.uiState.collectAsState()
    val currencySymbol = CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))

    var selectedClientId by remember { mutableStateOf("") }
    var showAddClientDialog by remember { mutableStateOf(false) }
    var clientError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var discountType by remember { mutableStateOf("AMOUNT") }
    var discountValue by remember { mutableDoubleStateOf(0.0) }

    var status by remember { mutableStateOf("PENDING") }
    var paidAmount by remember { mutableDoubleStateOf(0.0) }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var referenceNumber by remember { mutableStateOf("") }

    var notes by remember { mutableStateOf("") }
    var customFields by remember { mutableStateOf(mapOf<String, String>()) }
    var additionalExpanded by remember { mutableStateOf(false) }

    val hasGstin = gstEnabled && gstin.isNotBlank()

    // Pricing math — same as CreateOrderUiState
    val pricingMode = TaxCalculator.pricingModeOf(gstPricingMode)
    val gstTypeEnum = TaxCalculator.gstTypeOf(gstType)
    val itemBreakdowns = remember(uiState.cartItems, gstPricingMode, gstRate, gstType, hasGstin) {
        uiState.cartItems.map { item ->
            TaxCalculator.calculateLine(
                unitPrice = item.price,
                quantity = item.quantity,
                pricingMode = pricingMode,
                gstRate = gstRate,
                gstType = gstTypeEnum,
                hasGstin = hasGstin
            )
        }
    }
    val orderDraftItems = remember(uiState.cartItems) {
        uiState.cartItems.mapIndexed { index, item ->
            OrderItemDraft(
                id = item.id.ifBlank { "cart_$index" },
                productId = item.productId,
                productName = item.productName,
                quantity = item.quantity,
                unitPrice = item.price
            )
        }
    }
    val orderTotals = remember(uiState.cartItems, itemBreakdowns, discountType, discountValue, pricingMode) {
        if (orderDraftItems.isNotEmpty()) {
            TaxCalculator.calculateOrder(
                items = orderDraftItems.zip(itemBreakdowns),
                discountType = discountType,
                discountValue = discountValue,
                pricingMode = pricingMode
            )
        } else null
    }

    val subtotal = remember(uiState.cartItems) { uiState.cartItems.sumOf { it.price * it.quantity } }
    val discountAmount = orderTotals?.discountAmount ?: 0.0
    val taxableAmount = orderTotals?.taxableAmount ?: subtotal
    val totalCgst = orderTotals?.totalCgst ?: 0.0
    val totalSgst = orderTotals?.totalSgst ?: 0.0
    val totalIgst = orderTotals?.totalIgst ?: 0.0
    val totalTax = orderTotals?.totalTax ?: 0.0
    val grandTotal = orderTotals?.grandTotal ?: subtotal
    val roundedGrandTotal = orderTotals?.roundedGrandTotal ?: Math.round(subtotal)
    val roundOffAmount = orderTotals?.roundOffAmount ?: 0.0

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            showSuccessDialog = true
            cartViewModel.resetCheckoutSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            cartViewModel.clearError()
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
                    Text("OK", color = MaterialTheme.colorScheme.primary)
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
        bottomBar = {
            androidx.compose.material3.BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Payable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "$currencySymbol$roundedGrandTotal",
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        )
                    }
                    Button(
                        onClick = {
                            clientError = selectedClientId.isBlank()
                            if (!clientError && uiState.cartItems.isNotEmpty()) {
                                val allCustomFields = customFields.toMutableMap()
                                if (referenceNumber.isNotBlank()) {
                                    allCustomFields["referenceNumber"] = referenceNumber
                                }
                                if (discountValue > 0) {
                                    allCustomFields["discountType"] = discountType
                                    allCustomFields["discountValue"] = discountValue.toString()
                                }
                                if (hasGstin) {
                                    allCustomFields["gstRate"] = gstRate.toString()
                                    allCustomFields["gstType"] = gstType
                                    allCustomFields["gstPricingMode"] = gstPricingMode
                                }

                                cartViewModel.checkout(
                                    clientId = selectedClientId,
                                    notes = notes.trim(),
                                    status = status,
                                    paidAmount = paidAmount,
                                    paymentMethod = paymentMethod,
                                    discountType = discountType,
                                    discountValue = discountValue,
                                    referenceNumber = referenceNumber,
                                    grandTotal = roundedGrandTotal.toDouble(),
                                    hasGstin = hasGstin,
                                    gstRate = gstRate,
                                    gstPricingMode = gstPricingMode,
                                    gstType = gstType,
                                    customFields = allCustomFields
                                )
                            }
                        },
                        enabled = !uiState.isCheckingOut && uiState.cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isCheckingOut) {
                            ButtonLoadingIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Place Order", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            // Section 1: Client
            SectionCard(
                title = "Client",
                icon = Icons.Filled.People,
                subtitle = "Select a client for this order"
            ) {
                ClientPickerField(
                    clients = clients,
                    selectedClient = clients.find { it.id == selectedClientId },
                    onSelect = { client ->
                        selectedClientId = client.id
                        clientError = false
                    },
                    onClear = { selectedClientId = "" },
                    onCreateNew = { showAddClientDialog = true }
                )
                if (clientError) {
                    Text(
                        "Please select a client",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Section 2: Order Items (read-only cart)
            SectionCard(
                title = "Order Items",
                icon = Icons.Filled.ShoppingBag,
                subtitle = "Items from your cart"
            ) {
                if (uiState.cartItems.isEmpty()) {
                    Text(
                        "Cart is empty",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.cartItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${item.productName} × ${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = CurrencyUtils.formatCurrency(item.price * item.quantity, CurrencyUtils.loadCurrencyCode(context)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Section 3: Pricing Summary
            SectionCard(
                title = "Pricing",
                icon = Icons.Filled.Calculate,
                subtitle = "Order summary and tax calculation"
            ) {
                PricingSummaryCard(
                    subtotal = subtotal,
                    discountAmount = discountAmount,
                    taxableAmount = taxableAmount,
                    totalCgst = totalCgst,
                    totalSgst = totalSgst,
                    totalIgst = totalIgst,
                    totalTax = totalTax,
                    grandTotal = grandTotal,
                    roundedGrandTotal = roundedGrandTotal,
                    roundOffAmount = roundOffAmount,
                    hasGstin = hasGstin,
                    gstType = gstType,
                    gstRate = gstRate,
                    currencySymbol = currencySymbol,
                    discountType = discountType,
                    discountValue = discountValue,
                    onDiscountChange = { type, value ->
                        discountType = type
                        discountValue = value
                    }
                )
            }

            // Section 4: Payment
            SectionCard(
                title = "Payment",
                icon = Icons.Filled.Wallet,
                subtitle = "Record payment details for this order"
            ) {
                PaymentSection(
                    status = status,
                    onStatusChange = { status = it },
                    paidAmount = paidAmount,
                    onPaidAmountChange = { paidAmount = it },
                    paymentMethod = paymentMethod,
                    onPaymentMethodChange = { paymentMethod = it },
                    referenceNumber = referenceNumber,
                    onReferenceChange = { referenceNumber = it },
                    grandTotal = grandTotal,
                    currencySymbol = currencySymbol
                )
            }

            // Section 5: Additional Details (collapsible)
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
                    value = notes,
                    onValueChange = { if (it.length <= 300) notes = it },
                    placeholder = { Text("Write a note...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6,
                    supportingText = { Text("${notes.length}/300") },
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

                val entriesList = customFields.entries.toList()
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
                                        customFields.entries.forEachIndexed { i, (key, value) ->
                                            if (i == index) newMap[newKey] = value
                                            else newMap[key] = value
                                        }
                                        customFields = newMap
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
                                        customFields = customFields + (k to newValue)
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
                                    customFields = customFields - k
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        customFields = customFields + ("" to "")
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

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
