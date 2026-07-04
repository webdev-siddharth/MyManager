package com.core2studio.mymanager.ui.screens.categories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.data.local.entity.Category
import com.core2studio.mymanager.theme.DeepSlate
import com.core2studio.mymanager.theme.ForestGreen
import com.core2studio.mymanager.theme.MintCream
import com.core2studio.mymanager.theme.SageGreen
import com.core2studio.mymanager.ui.components.EmptyState
import com.core2studio.mymanager.ui.components.MyManagerCard
import com.core2studio.mymanager.ui.components.MyManagerFAB
import com.core2studio.mymanager.ui.components.MyManagerTopBar

@Composable
fun CategoriesScreen(
    onCategoryClick: (String) -> Unit,
    onCartClick: () -> Unit = {},
    categories: List<Category> = emptyList(),
    productCountResolver: (String) -> Int = { 0 },
    cartCount: Int = 0,
    onAddCategory: (name: String, description: String) -> Unit = { _, _ -> },
    onUpdateCategory: (Category) -> Unit = {},
    onDeleteCategory: (Category) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            MyManagerTopBar(
                title = "Catalog",
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
                                tint = ForestGreen
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            com.core2studio.mymanager.ui.components.MyManagerFAB(
                onClick = { showAddDialog = true },
                icon = Icons.Filled.Add,
                text = "Add Category"
            )
        },
        containerColor = MintCream
    ) { innerPadding ->
        if (categories.isEmpty()) {
            com.core2studio.mymanager.ui.components.EmptyState(
                message = "No categories yet. Add your first category!",

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
                    items = categories,
                    key = { it.id }
                ) { category ->
                    Box {
                        com.core2studio.mymanager.ui.components.MyManagerCard(
                            onClick = { onCategoryClick(category.id) }
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = DeepSlate,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = category.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = DeepSlate.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${productCountResolver(category.id)} products",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageGreen
                            )
                        }

                        IconButton(
                            onClick = { expandedCategoryId = category.id },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More options",
                                tint = DeepSlate
                            )
                        }

                        DropdownMenu(
                            expanded = expandedCategoryId == category.id,
                            onDismissRequest = { expandedCategoryId = null }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    expandedCategoryId = null
                                    editingCategory = category
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = ForestGreen
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = androidx.compose.ui.graphics.Color.Red) },
                                onClick = {
                                    expandedCategoryId = null
                                    showDeleteDialog = category
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.Red
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            com.core2studio.mymanager.ui.screens.categories.AddCategoryDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, description ->
                    onAddCategory(name, description)
                    showAddDialog = false
                }
            )
        }

        editingCategory?.let { category ->
            com.core2studio.mymanager.ui.screens.categories.AddCategoryDialog(
                onDismiss = { editingCategory = null },
                onConfirm = { name, description ->
                    onUpdateCategory(category.copy(name = name, description = description))
                    editingCategory = null
                },
                editCategory = category
            )
        }

        showDeleteDialog?.let { category ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Category") },
                text = { Text("Are you sure you want to delete \"${category.name}\"? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDeleteCategory(category)
                        showDeleteDialog = null
                    }) {
                        Text("Delete", color = androidx.compose.ui.graphics.Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
