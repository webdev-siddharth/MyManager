package com.core2studio.mymanager.ui.screens.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.data.utils.CurrencyUtils

@Composable
fun ProductDetailScreen(
    product: Product?,
    onBack: () -> Unit,
    onUpdateProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onAddImages: (List<Uri>) -> Unit,
    onRemoveImage: (String) -> Unit,
    onAddToCart: (Product, Int) -> Unit = { _, _ -> },
    isUploading: Boolean = false
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(1) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            onAddImages(uris)
        }
    }

    LaunchedEffect(product) {
        product?.let {
            editName = it.name
            editDescription = it.description
            editPrice = it.price.toString()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete this product?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        product?.let { onDeleteProduct(it) }
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            com.core2studio.mymanager.ui.components.MyManagerTopBar(
                title = if (isEditing) "Edit Product" else "Product Details",
                onBackClick = {
                    if (isEditing) {
                        isEditing = false
                        product?.let {
                            editName = it.name
                            editDescription = it.description
                            editPrice = it.price.toString()
                        }
                    } else {
                        onBack()
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isEditing) {
                FloatingActionButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add images"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image carousel
                if (product.imageUrls.isNotEmpty()) {
                    val pagerState = rememberPagerState(pageCount = { product.imageUrls.size })

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = product.imageUrls[page],
                                    contentDescription = "Product image ${page + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isEditing) {
                                    IconButton(
                                        onClick = { onRemoveImage(product.imageUrls[page]) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove image",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Page indicator
                        if (product.imageUrls.size > 1) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                repeat(product.imageUrls.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == pagerState.currentPage) MaterialTheme.colorScheme.surface
                                                else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // No images placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "No images",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No images yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add images",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                // Product info
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Product Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        OutlinedTextField(
                            value = editDescription,
                            onValueChange = { editDescription = it },
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

                        OutlinedTextField(
                            value = editPrice,
                            onValueChange = { editPrice = it },
                            label = { Text("Price (${CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))})") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            prefix = { Text(CurrencyUtils.getCurrencySymbol(CurrencyUtils.loadCurrencyCode(context))) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Button(
                            onClick = {
                                val priceValue = editPrice.toDoubleOrNull()
                                if (editName.isNotBlank() && priceValue != null && priceValue > 0) {
                                    onUpdateProduct(
                                        product.copy(
                                            name = editName.trim(),
                                            description = editDescription.trim(),
                                            price = priceValue
                                        )
                                    )
                                    isEditing = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isUploading) "Saving..." else "Save Changes",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(context)),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (product.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        // Quantity selector
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Quantity",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { if (quantity > 1) quantity-- },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Remove,
                                    contentDescription = "Decrease quantity",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "$quantity",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { quantity++ },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Increase quantity",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Add to Cart button
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onAddToCart(product, quantity) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add to Cart",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
