package com.core2studio.mymanager.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.ui.components.AccountMenuItemCard
import com.core2studio.mymanager.ui.components.AccountMenuItemVariant
import com.core2studio.mymanager.ui.components.MyManagerCard

@Composable
fun SettingsScreen(
    onBusinessInfoClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    accountEmail: String? = null,
    message: String? = null,
    onSignOutApp: () -> Unit = {}
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Large header
            Column {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage your account and business settings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Google Account (highlighted)
            AccountMenuItemCard(
                icon = Icons.Filled.Person,
                title = "Account",
                subtitle = accountEmail ?: "Not signed in",
                showVerifiedBadge = accountEmail != null,
                variant = AccountMenuItemVariant.Highlighted,
                onClick = null
            )

            // Profile
            AccountMenuItemCard(
                icon = Icons.Filled.Person,
                title = "Profile",
                subtitle = "Manage your personal information",
                variant = AccountMenuItemVariant.Default,
                onClick = onProfileClick
            )

            // Business Info
            AccountMenuItemCard(
                icon = Icons.Filled.Store,
                title = "Business Info",
                subtitle = "Manage your business details",
                variant = AccountMenuItemVariant.Default,
                onClick = onBusinessInfoClick
            )

            // Settings
            AccountMenuItemCard(
                icon = Icons.Filled.Settings,
                title = "Settings",
                subtitle = "App preferences and configurations",
                variant = AccountMenuItemVariant.Default,
                onClick = onSettingsClick
            )

            // Message display
            AnimatedVisibility(visible = message != null) {
                MyManagerCard {
                    Text(
                        text = message ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message?.contains(
                                "failed",
                                ignoreCase = true
                            ) == true
                        ) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider()

            // Sign Out (danger)
            AccountMenuItemCard(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign Out",
                subtitle = "Log out from your account",
                variant = AccountMenuItemVariant.Danger,
                compact = true,
                onClick = { showSignOutDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOutApp()
                    }
                ) {
                    Text("Sign Out", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
