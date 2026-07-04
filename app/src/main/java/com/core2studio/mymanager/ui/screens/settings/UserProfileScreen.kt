package com.core2studio.mymanager.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ErrorRed
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@Composable
fun UserProfileScreen(
    displayName: String = "",
    email: String = "",
    isLoading: Boolean = false,
    message: String? = null,
    onUpdateDisplayName: (String) -> Unit = {},
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> },
    onClearMessage: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var isEditMode by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var localDisplayName by remember { mutableStateOf(displayName) }

    LaunchedEffect(displayName) {
        localDisplayName = displayName
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    val userInitial = displayName.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = if (isEditMode) "Edit Profile" else "Profile",
                onBackClick = {
                    if (isEditMode) {
                        isEditMode = false
                        localDisplayName = displayName
                    } else {
                        onBack()
                    }
                }
            )
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

            // Avatar
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ForestGreen)
                        .border(3.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInitial,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }

            if (isEditMode) {
                // EDIT MODE
                MyManagerCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = ForestGreen
                        )
                        Text(
                            text = "Edit Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = localDisplayName,
                        onValueChange = { localDisplayName = it },
                        label = { Text("Display Name*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors,
                        placeholder = { Text("Enter your name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = SageGreen,
                            disabledLabelColor = SageGreen.copy(alpha = 0.6f),
                            disabledTextColor = DeepSlate,
                            disabledPlaceholderColor = DeepSlate.copy(alpha = 0.4f)
                        ),
                        enabled = false
                    )
                }

                Button(
                    onClick = {
                        onUpdateDisplayName(localDisplayName)
                        isEditMode = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ForestGreen,
                        contentColor = White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes")
                }
            } else {
                // VIEW MODE
                MyManagerCard {
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
                            text = "Account Details",
                            style = MaterialTheme.typography.titleMedium,
                            color = DeepSlate
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(icon = Icons.Filled.Person, label = "Name", value = displayName.ifEmpty { "Not set" })
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(icon = Icons.Filled.Email, label = "Email", value = email.ifEmpty { "Not set" })
                }

                // Message display
                if (message != null) {
                    MyManagerCard {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (message.contains("failed", ignoreCase = true) ||
                                message.contains("incorrect", ignoreCase = true)
                            ) ErrorRed else ForestGreen
                        )
                    }
                }

                // Edit Profile Button
                OutlinedButton(
                    onClick = { isEditMode = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }

                // Change Password Button
                OutlinedButton(
                    onClick = { showPasswordDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        ChangePasswordDialog(
            isLoading = isLoading,
            serverError = message,
            onDismiss = {
                showPasswordDialog = false
                onClearMessage()
            },
            onConfirm = { currentPassword, newPassword ->
                onChangePassword(currentPassword, newPassword)
            }
        )
    }
}

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean = false,
    serverError: String? = null,
    onDismiss: () -> Unit,
    onConfirm: (currentPassword: String, newPassword: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    // Show server error inside the dialog
    val displayError = error ?: serverError

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ForestGreen,
        unfocusedBorderColor = SageGreen,
        cursorColor = ForestGreen,
        focusedLabelColor = ForestGreen
    )

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; error = null },
                    label = { Text("Current Password*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it; error = null },
                    label = { Text("New Password*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; error = null },
                    label = { Text("Confirm New Password*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = textFieldColors,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !isLoading
                )
                if (displayError != null) {
                    Text(
                        text = displayError,
                        style = MaterialTheme.typography.bodySmall,
                        color = ErrorRed
                    )
                }
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = ForestGreen)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    error = null
                    when {
                        currentPassword.isBlank() -> error = "Current password is required"
                        newPassword.isBlank() -> error = "New password is required"
                        newPassword.length < 6 -> error = "Password must be at least 6 characters"
                        newPassword != confirmPassword -> error = "Passwords do not match"
                        else -> onConfirm(currentPassword, newPassword)
                    }
                },
                enabled = !isLoading
            ) {
                Text("Update", color = ForestGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SageGreen,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SageGreen
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = DeepSlate
            )
        }
    }
}
