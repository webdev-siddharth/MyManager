package com.core2studio.mymanager.ui.screens.orders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.SearchBar
import com.core2studio.mymanager.ui.components.StatusChip
import com.core2studio.mymanager.ui.components.highlightText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
    onAddOrder: () -> Unit,
    onOrderClick: (String) -> Unit,
    orders: List<Order> = emptyList(),
    searchQuery: String = "",
    filteredOrders: List<Order> = emptyList(),
    onSearchQueryChange: (String) -> Unit = {},
    clientNameResolver: (String) -> String = { "Unknown Client" }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var filterState by remember { mutableStateOf(OrderFilterState()) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val displayOrders = remember(filteredOrders, filterState) {
        var result = filteredOrders

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
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    BadgedBox(
                        badge = {
                            if (filterState.isActive) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
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
                                tint = MaterialTheme.colorScheme.onSurface
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onClear = {
                        onSearchQueryChange("")
                        showSearch = false
                    },
                    placeholder = "Search orders...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            AnimatedVisibility(visible = displayOrders.isEmpty()) {
                EmptyState(
                    message = when {
                        searchQuery.isNotBlank() -> "No orders found"
                        filterState.isActive -> "No matching orders"
                        else -> "No orders yet"
                    },
                )
            }

            AnimatedVisibility(visible = displayOrders.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = displayOrders,
                        key = { it.id }
                    ) { order ->
                        MyManagerCard(
                            onClick = { onOrderClick(order.id) }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = highlightText(clientNameResolver(order.clientId), searchQuery),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyUtils.formatCurrency(order.amount, CurrencyUtils.loadCurrencyCode(context)),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (order.paidAmount < order.amount) {
                                        Text(
                                            text = "Paid: ${CurrencyUtils.formatCurrency(order.paidAmount, CurrencyUtils.loadCurrencyCode(context))}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dateFormatter.format(Date(order.date)),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (order.productName.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = highlightText(order.productName, searchQuery),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusChip(status = order.status)
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
}
