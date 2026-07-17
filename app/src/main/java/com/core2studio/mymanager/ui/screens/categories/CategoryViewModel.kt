package com.core2studio.mymanager.ui.screens.categories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.firestore.FirestoreCategoryRepository
import com.core2studio.mymanager.data.local.entity.Category
import com.core2studio.mymanager.data.repository.CategoryRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val productCounts: Map<String, Int> = emptyMap(),
    val showAddDialog: Boolean = false,
    val editingCategory: Category? = null,
    val errorMessage: String? = null
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val firestoreCategoryRepository: FirestoreCategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
                val counts = mutableMapOf<String, Int>()
                categories.forEach { category ->
                    val products = productRepository.getProductsByCategoryOnce(category.id)
                    counts[category.id] = products.size
                }
                _uiState.value = _uiState.value.copy(productCounts = counts)
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingCategory = null)
    }

    fun showEditDialog(category: Category) {
        _uiState.value = _uiState.value.copy(showAddDialog = true, editingCategory = category)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false, editingCategory = null)
    }

    fun addCategory(name: String, description: String) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreCategoryRepository.insertCategory(
                uid,
                Category(name = name, description = description)
            ).onFailure { e ->
                Log.e(TAG, "Failed to add category", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save category: ${e.message}")
            }
            dismissDialog()
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreCategoryRepository.updateCategory(uid, category).onFailure { e ->
                Log.e(TAG, "Failed to update category", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update category: ${e.message}")
            }
            dismissDialog()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreCategoryRepository.deleteCategory(uid, category).onFailure { e ->
                Log.e(TAG, "Failed to delete category", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete category: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        private const val TAG = "CategoryViewModel"
    }
}
