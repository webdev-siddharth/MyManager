package com.core2studio.mymanager.ui.screens.clients

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.StatusChip
import com.core2studio.mymanager.data.utils.CurrencyUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ClientDetailScreen(
    clientId: String,
    onBack: () -> Unit,
    client: Client? = null,
    transactions: List<Order> = emptyList(),
    onGenerateInvoice: (Order) -> Unit = {},
    invoiceMessage: String? = null,
    onClearMessage: () -> Unit = {},
    invoiceUri: Uri? = null,
    onClearUri: () -> Unit = {},
    onEditClient: () -> Unit = {},
    showEditDialog: Boolean = false,
    editClient: Client? = null,
    onDismissEditDialog: () -> Unit = {},
    onConfirmEdit: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> }
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(invoiceMessage) {
        if (invoiceMessage != null) {
            Toast.makeText(context, invoiceMessage, Toast.LENGTH_LONG).show()
            onClearMessage()
        }
    }

    LaunchedEffect(invoiceUri) {
        if (invoiceUri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(invoiceUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            onClearUri()
        }
    }

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = client?.name ?: "Client Details",
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = onEditClient) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Client",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Client info card
            if (client != null) {
                item {
                    com.core2studio.mymanager.ui.components.MyManagerCard {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (client.phone.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = client.phone,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (client.email.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = client.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (client.address.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = client.address,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Transaction History Header
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Order History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (transactions.isEmpty()) {
                item {
                    com.core2studio.mymanager.ui.components.EmptyState(message = "No orders with this client")
                }
            } else {
                items(
                    items = transactions,
                    key = { it.id }
                ) { order ->
                    com.core2studio.mymanager.ui.components.MyManagerCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = CurrencyUtils.formatCurrency(order.amount, CurrencyUtils.loadCurrencyCode(context)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (order.paidAmount < order.amount) {
                                    Text(
                                        text = "Paid: ${CurrencyUtils.formatCurrency(order.paidAmount, CurrencyUtils.loadCurrencyCode(context))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateFormatter.format(Date(order.date)),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                com.core2studio.mymanager.ui.components.StatusChip(
                                    status = order.status
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { onGenerateInvoice(order) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Description,
                                        contentDescription = null,
                                        modifier = Modifier.height(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Invoice", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showEditDialog && editClient != null) {
        EditClientDialog(
            client = editClient,
            onDismiss = onDismissEditDialog,
            onConfirm = onConfirmEdit
        )
    }
}
