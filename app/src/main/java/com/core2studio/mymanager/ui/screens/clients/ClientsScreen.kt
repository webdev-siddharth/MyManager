package com.core2studio.mymanager.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
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
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.ui.components.SearchBar
import com.core2studio.mymanager.ui.components.highlightText

@Composable
fun ClientsScreen(
    onClientClick: (String) -> Unit,
    clients: List<Client> = emptyList(),
    searchQuery: String = "",
    filteredClients: List<Client> = emptyList(),
    onSearchQueryChange: (String) -> Unit = {},
    showAddDialog: Boolean = false,
    onShowAddDialog: () -> Unit = {},
    onDismissAddDialog: () -> Unit = {},
    onAddClient: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> },
    errorMessage: String? = null,
    onDismissError: () -> Unit = {}
) {
    var arrangeOption by remember { mutableStateOf(ClientArrangeOption.RECENT) }
    var showArrangeSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }

    val arrangedClients = remember(filteredClients, arrangeOption) {
        when (arrangeOption) {
            ClientArrangeOption.RECENT -> filteredClients.sortedByDescending { it.createdAt }
            ClientArrangeOption.A_Z -> filteredClients.sortedBy { it.name.lowercase() }
            ClientArrangeOption.Z_A -> filteredClients.sortedByDescending { it.name.lowercase() }
        }
    }

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = "Clients",
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showArrangeSheet = true }) {
                        Icon(
                            imageVector = Icons.Filled.SwapVert,
                            contentDescription = "Arrange",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            com.core2studio.mymanager.ui.components.MyManagerFAB(
                onClick = onShowAddDialog,
                icon = Icons.Filled.Add,
                text = "New Client"
            )
        },
        snackbarHost = {
            com.core2studio.mymanager.ui.components.ErrorSnackbarHost(
                errorMessage = errorMessage,
                onDismissError = onDismissError
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
                    placeholder = "Search clients...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (arrangedClients.isEmpty()) {
                if (searchQuery.isNotBlank()) {
                    com.core2studio.mymanager.ui.components.EmptyState(
                        message = "No clients found",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    com.core2studio.mymanager.ui.components.EmptyState(
                        message = "No clients yet",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    items(
                        items = arrangedClients,
                        key = { it.id }
                    ) { client ->
                        com.core2studio.mymanager.ui.components.MyManagerCard(
                            onClick = { onClientClick(client.id) }
                        ) {
                            Text(
                                text = highlightText(client.name, searchQuery),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (client.phone.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.height(16.dp)
                                    )
                                    Text(
                                        text = highlightText(client.phone, searchQuery),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (client.email.isNotBlank()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Filled.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.height(16.dp)
                                    )
                                    Text(
                                        text = highlightText(client.email, searchQuery),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddDialog) {
            com.core2studio.mymanager.ui.screens.clients.AddClientDialog(
                onDismiss = onDismissAddDialog,
                onConfirm = onAddClient
            )
        }

        if (showArrangeSheet) {
            ClientArrangeBottomSheet(
                currentOption = arrangeOption,
                onDismiss = { showArrangeSheet = false },
                onApply = { arrangeOption = it },
                onReset = { arrangeOption = ClientArrangeOption.RECENT }
            )
        }
    }
}
