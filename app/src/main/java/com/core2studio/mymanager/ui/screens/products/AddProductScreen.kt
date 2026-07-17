package com.core2studio.mymanager.ui.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.core2studio.mymanager.data.utils.CurrencyUtils
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddProductScreen(
    categoryId: String,
    onBack: () -> Unit,
    onSave: (name: String, description: String, price: Double, imageUris: List<String>) -> Unit = { _, _, _, _ -> }
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        selectedImageUris = selectedImageUris + uris
    }

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = "Add Product",
                onBackClick = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUris.isNotEmpty()) {
                    AsyncImage(
                        model = selectedImageUris.first(),
                        contentDescription = "Selected product image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to add product images",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            // Show selected image count and thumbnails
            if (selectedImageUris.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedImageUris.forEachIndexed { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Image ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove image",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant)
                                    .clickable {
                                        selectedImageUris = selectedImageUris.toMutableList().apply {
                                            removeAt(index)
                                        }
                                    }
                                    .padding(2.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    // Add more button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Add more images",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Text(
                    text = "${selectedImageUris.size} image(s) selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // Name
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Name is required") }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            // Price
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it
                    priceError = false
                },
                label = { Text("Price (${CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))})") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = priceError,
                supportingText = if (priceError) {
                    { Text("Valid price is required") }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text(CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    val isNameValid = name.isNotBlank()
                    val priceValue = price.toDoubleOrNull()
                    val isPriceValid = priceValue != null && priceValue > 0

                    nameError = !isNameValid
                    priceError = !isPriceValid

                    if (isNameValid && isPriceValid) {
                        onSave(
                            name.trim(),
                            description.trim(),
                            priceValue!!,
                            selectedImageUris.map { it.toString() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    text = "Save Product",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
