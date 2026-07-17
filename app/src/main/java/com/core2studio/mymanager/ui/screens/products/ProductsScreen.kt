package com.core2studio.mymanager.ui.screens.products

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar
import com.core2studio.mymanager.data.utils.CurrencyUtils

@Composable
fun ProductsScreen(
    categoryId: String,
    onBack: () -> Unit,
    onAddProduct: (String) -> Unit,
    onProductClick: (String) -> Unit = {},
    onEditProduct: (String) -> Unit = {},
    onAddToCart: (Product) -> Unit = {},
    onCartClick: () -> Unit = {},
    onShareProduct: (Product) -> Unit = {},
    onShareProducts: (List<Product>) -> Unit = {},
    products: List<Product> = emptyList(),
    categoryName: String = "Products",
    cartCount: Int = 0,
    onDeleteProduct: (Product) -> Unit = {},
    onDeleteProducts: (List<Product>) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var expandedProductId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedProductIds by remember { mutableStateOf(setOf<String>()) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    val selectedProducts = remember(selectedProductIds, products) {
        products.filter { it.id in selectedProductIds }
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                MyManagerTopBar(
                    title = "${selectedProductIds.size} selected",
                    onBackClick = {
                        isSelectionMode = false
                        selectedProductIds = emptySet()
                    },
                    actions = {
                        IconButton(onClick = {
                            selectedProductIds = products.map { it.id }.toSet()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.SelectAll,
                                contentDescription = "Select All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onShareProducts(selectedProducts) }) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Share selected",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showBatchDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            } else {
                MyManagerTopBar(
                    title = categoryName,
                    onBackClick = onBack,
                    actions = {
                        IconButton(onClick = onCartClick) {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge {
                                            Text(text = "$cartCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                MyManagerFAB(
                    onClick = { onAddProduct(categoryId) },
                    icon = Icons.Filled.Add,
                    text = "Add Product"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (products.isEmpty()) {
            EmptyState(
                message = "No products in this category yet",
//                actionLabel = "Add Product",
//                onAction = { onAddProduct(categoryId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = products,
                    key = { it.id }
                ) { product ->
                    val isSelected = product.id in selectedProductIds

                    Box(
                        modifier = if (isSelectionMode && isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                        } else {
                            Modifier
                        }
                    ) {
                        MyManagerCard(
                            onClick = {
                                if (isSelectionMode) {
                                    selectedProductIds = if (isSelected) {
                                        selectedProductIds - product.id
                                    } else {
                                        selectedProductIds + product.id
                                    }
                                    if (selectedProductIds.isEmpty()) {
                                        isSelectionMode = false
                                    }
                                } else {
                                    onProductClick(product.id)
                                }
                            }
                        ) {
                            if (product.imageUrls.isNotEmpty()) {
                                AsyncImage(
                                    model = product.imageUrls.first(),
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = "No image",
                                        tint = MaterialTheme.colorScheme.outlineVariant,
                                        modifier = Modifier.fillMaxSize(0.4f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = CurrencyUtils.formatCurrency(product.price, CurrencyUtils.loadCurrencyCode(context)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (!isSelectionMode) {
                                    FloatingActionButton(
                                        onClick = { onAddToCart(product) },
                                        modifier = Modifier.size(32.dp),
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        shape = CircleShape,
                                        elevation = FloatingActionButtonDefaults.elevation(
                                            defaultElevation = 0.dp,
                                            pressedElevation = 0.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ShoppingCart,
                                            contentDescription = "Add to cart",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (isSelectionMode) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    selectedProductIds = if (checked) {
                                        selectedProductIds + product.id
                                    } else {
                                        selectedProductIds - product.id
                                    }
                                    if (selectedProductIds.isEmpty()) {
                                        isSelectionMode = false
                                    }
                                },
                                modifier = Modifier.align(Alignment.TopStart),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        } else {
                            Surface(
                                onClick = { expandedProductId = product.id },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shadowElevation = 2.dp
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "More options",
                                    modifier = Modifier.padding(6.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            DropdownMenu(
                                expanded = expandedProductId == product.id,
                                onDismissRequest = { expandedProductId = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        expandedProductId = null
                                        onEditProduct(product.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        expandedProductId = null
                                        showDeleteDialog = product
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Select") },
                                    onClick = {
                                        expandedProductId = null
                                        isSelectionMode = true
                                        selectedProductIds = setOf(product.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.CheckBox,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = {
                                        expandedProductId = null
                                        onShareProduct(product)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { product ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete \"${product.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProduct(product)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBatchDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDeleteDialog = false },
            title = { Text("Delete Products") },
            text = {
                Text(
                    "Are you sure you want to delete ${selectedProducts.size} product(s)? This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteProducts(selectedProducts)
                    selectedProductIds = emptySet()
                    isSelectionMode = false
                    showBatchDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
