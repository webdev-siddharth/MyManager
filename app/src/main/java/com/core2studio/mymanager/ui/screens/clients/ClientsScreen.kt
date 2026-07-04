package com.core2studio.mymanager.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@Composable
fun ClientsScreen(
    onClientClick: (String) -> Unit,
    clients: List<Client> = emptyList(),
    showAddDialog: Boolean = false,
    onShowAddDialog: () -> Unit = {},
    onDismissAddDialog: () -> Unit = {},
    onAddClient: (name: String, phone: String, email: String, address: String) -> Unit = { _, _, _, _ -> }
) {
    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(title = "Clients")
        },
        floatingActionButton = {
            com.core2studio.mymanager.ui.components.MyManagerFAB(
                onClick = onShowAddDialog,
                icon = Icons.Filled.Add,
                text = "New Client"
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (clients.isEmpty()) {
            com.core2studio.mymanager.ui.components.EmptyState(
                message = "No clients yet",

                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(
                    items = clients,
                    key = { it.id }
                ) { client ->
                    com.core2studio.mymanager.ui.components.MyManagerCard(
                        onClick = { onClientClick(client.id) }
                    ) {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (client.phone.isNotBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.height(16.dp)
                                )
                                Text(
                                    text = client.phone,
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
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.height(16.dp)
                                )
                                Text(
                                    text = client.email,
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

        if (showAddDialog) {
            com.core2studio.mymanager.ui.screens.clients.AddClientDialog(
                onDismiss = onDismissAddDialog,
                onConfirm = onAddClient
            )
        }
    }
}
