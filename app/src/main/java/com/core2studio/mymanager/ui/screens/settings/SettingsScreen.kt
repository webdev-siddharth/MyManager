package com.core2studio.mymanager.ui.screens.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ErrorRed
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@Composable
fun SettingsScreen(
    onBusinessInfoClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    accountEmail: String? = null,
    message: String? = null,
    onSignOutApp: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(title = "Account")
        },
        containerColor = MintCream
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

            // Google Account Section
            com.core2studio.mymanager.ui.components.MyManagerCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (accountEmail != null) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = if (accountEmail != null) ForestGreen else SageGreen
                    )
                    Text(
                        text = "Google Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSlate
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (accountEmail != null) {
                    Text(
                        text = accountEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SageGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Text(
                        text = "Not signed in",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepSlate.copy(alpha = 0.6f)
                    )
                }
            }

            // Profile Section
            com.core2studio.mymanager.ui.components.MyManagerCard(
                onClick = onProfileClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = ForestGreen
                    )
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSlate
                    )
                }
            }

            // Business Info Section
            com.core2studio.mymanager.ui.components.MyManagerCard(
                onClick = onBusinessInfoClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Store,
                        contentDescription = null,
                        tint = ForestGreen
                    )
                    Text(
                        text = "Business Info",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSlate
                    )
                }
            }



            // Message display
            AnimatedVisibility(visible = message != null) {
                com.core2studio.mymanager.ui.components.MyManagerCard {
                    Text(
                        text = message ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message?.contains(
                                "failed",
                                ignoreCase = true
                            ) == true
                        ) ErrorRed else ForestGreen
                    )
                }
            }

            // About Section
            com.core2studio.mymanager.ui.components.MyManagerCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = SageGreen
                    )
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSlate
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "MyManager",
                    style = MaterialTheme.typography.bodyLarge,
                    color = DeepSlate
                )
                Text(
                    text = "A business management app for business owners.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepSlate.copy(alpha = 0.6f)
                )
            }

            // Sign Out of App
            com.core2studio.mymanager.ui.components.MyManagerCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = ErrorRed
                    )
                    Text(
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = DeepSlate
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSignOutApp,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
