package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.core2studio.mymanager.theme.White
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    onAddOrder: () -> Unit,
    onClientClick: (String) -> Unit,
    orders: List<Order> = emptyList(),
    clientNameResolver: (String) -> String = { "Unknown Client" },
    onStatusUpdate: (Order) -> Unit = {}
) {
    var filterState by remember { mutableStateOf(OrderFilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }
    var selectedClientName by remember { mutableStateOf("") }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val filteredOrders = remember(orders, filterState) {
        var result = orders

        // Apply status filter
        if (filterState.status != "All") {
            result = result.filter { it.status.equals(filterState.status, ignoreCase = true) }
        }

        // Apply date filter
        result = applyDateFilter(result, filterState.dateOption, filterState.customStartDate, filterState.customEndDate)

        // Apply price sort
        result = applyPriceSort(result, filterState.priceSort)

        result
    }

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = "Orders",
                actions = {
                    BadgedBox(
                        badge = {
                            if (filterState.isActive) {
                                Badge(
                                    containerColor = ForestGreen,
                                    contentColor = White
                                ) {
                                    Text("!")
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = DeepSlate
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            MyManagerFAB(
                onClick = onAddOrder,
                icon = Icons.Filled.Add,
                text = "New Order"
            )
        },
        containerColor = MintCream
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(visible = filteredOrders.isEmpty()) {
                EmptyState(
                    message = if (filterState.isActive) {
                        "No matching orders"
                    } else {
                        "No orders yet"
                    },
                )
            }

            AnimatedVisibility(visible = filteredOrders.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredOrders,
                        key = { it.id }
                    ) { order ->
                        MyManagerCard(
                            onClick = { onClientClick(order.clientId) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = clientNameResolver(order.clientId),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DeepSlate
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "₹${
                                            String.format(
                                                Locale.getDefault(),
                                                "%.2f",
                                                order.amount
                                            )
                                        }",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = ForestGreen
                                    )
                                    if (order.paidAmount < order.amount) {
                                        Text(
                                            text = "Paid: ₹${
                                                String.format(
                                                    Locale.getDefault(),
                                                    "%.2f",
                                                    order.paidAmount
                                                )
                                            }",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = DeepSlate.copy(alpha = 0.6f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormatter.format(Date(order.date)),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DeepSlate.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    StatusChip(
                                        status = order.status
                                    )
                                    Box {
                                        IconButton(
                                            onClick = { expandedOrderId = order.id }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "More options",
                                                tint = ForestGreen
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = expandedOrderId == order.id,
                                            onDismissRequest = { expandedOrderId = null }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Edit Status") },
                                                onClick = {
                                                    expandedOrderId = null
                                                    selectedOrder = order
                                                    selectedClientName = clientNameResolver(order.clientId)
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Filled.Edit,
                                                        contentDescription = null,
                                                        tint = ForestGreen
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        OrderFilterBottomSheet(
            filterState = filterState,
            onDismiss = { showFilterSheet = false },
            onApply = { filterState = it },
            onReset = { filterState = OrderFilterState() }
        )
    }

    if (selectedOrder != null) {
        OrderDetailDialog(
            order = selectedOrder!!,
            clientName = selectedClientName,
            onDismiss = { selectedOrder = null },
            onStatusUpdate = onStatusUpdate
        )
    }
}
