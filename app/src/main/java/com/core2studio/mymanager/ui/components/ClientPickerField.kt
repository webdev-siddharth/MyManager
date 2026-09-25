package com.core2studio.mymanager.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Client

@Composable
fun ClientPickerField(
    clients: List<Client>,
    selectedClient: Client?,
    onSelect: (Client) -> Unit,
    onClear: () -> Unit,
    onCreateNew: () -> Unit,
    modifier: Modifier = Modifier,
    recentClientIds: List<String> = emptyList()
) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val recentClients = remember(clients, recentClientIds) {
        clients.filter { it.id in recentClientIds }
    }

    val otherClients = remember(clients, recentClientIds) {
        clients.filter { it.id !in recentClientIds }
    }

    val filteredClients = remember(otherClients, query) {
        derivedStateOf {
            if (query.isBlank()) otherClients else otherClients.filter { it.name.lowercase().contains(query.lowercase()) }
        }
    }.value

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedClient?.name ?: query,
            onValueChange = { query = it; expanded = true },
            label = { Text("Select Client") },
            placeholder = { if (selectedClient == null) Text("Choose a client...") },
            modifier = Modifier.fillMaxWidth().clickable {
                if (selectedClient != null) {
                    onClear()
                    query = ""
                }
                expanded = true
            },
            readOnly = selectedClient != null,
            enabled = false,
            trailingIcon = {
                if (selectedClient != null) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear client",
                        modifier = Modifier.size(24.dp).clickable {
                            onClear()
                            query = ""
                            expanded = true
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp).clickable { expanded = true }
                    )
                }
            },
            isError = selectedClient == null && query.isNotBlank() && filteredClients.isEmpty(),
            supportingText = if (selectedClient == null && query.isNotBlank() && filteredClients.isEmpty()) {
                { Text("No matches. Tap + to create new client.") }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Create New Client at TOP
            DropdownMenuItem(
                text = { Text("+ Create New Client", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) },
                onClick = { onCreateNew(); expanded = false }
            )
            HorizontalDivider()

            if (recentClients.isNotEmpty() && query.isBlank()) {
                DropdownMenuItem(
                    text = { Text("Recent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {}
                )
                recentClients.forEach { client ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text(client.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        },
                        onClick = { onSelect(client); expanded = false; query = client.name }
                    )
                }
                HorizontalDivider()
            }

            filteredClients.forEach { client ->
                DropdownMenuItem(
                    text = { Text(client.name, style = MaterialTheme.typography.bodyMedium) },
                    onClick = { onSelect(client); expanded = false; query = client.name }
                )
            }

            if (filteredClients.isEmpty() && query.isNotBlank()) {
                DropdownMenuItem(
                    text = { Text("No matches found", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {}
                )
            }
        }
    }
}