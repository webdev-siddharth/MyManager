package com.core2studio.mymanager.ui.screens.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.data.local.entity.Category
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.PaleMint
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.theme.White

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit,
    editCategory: Category? = null
) {
    var name by remember { mutableStateOf(editCategory?.name ?: "") }
    var description by remember { mutableStateOf(editCategory?.description ?: "") }
    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaleMint,
        tonalElevation = 0.dp,
        title = {
            Text(
                text = if (editCategory != null) "Edit Category" else "Add Category",
                style = MaterialTheme.typography.titleLarge,
                color = DeepSlate
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Name is required") }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = SageGreen,
                        cursorColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreen,
                        unfocusedBorderColor = SageGreen,
                        cursorColor = ForestGreen,
                        focusedLabelColor = ForestGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(name.trim(), description.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = White
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(text = if (editCategory != null) "Update" else "Add")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ForestGreen
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
