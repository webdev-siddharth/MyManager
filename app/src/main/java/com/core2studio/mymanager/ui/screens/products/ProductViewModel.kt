package com.core2studio.mymanager.ui.screens.products

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.firestore.FirestoreProductRepository
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.repository.CategoryRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import com.core2studio.mymanager.data.repository.ProductShareGenerator
import com.core2studio.mymanager.data.storage.CloudinaryStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class ProductUiState(
    val products: List<Product> = emptyList(),
    val categoryName: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: Product? = null,
    val errorMessage: String? = null
)

data class ShareUiState(
    val shareUri: Uri? = null,
    val isSharing: Boolean = false
)

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val firestoreProductRepository: FirestoreProductRepository,
    private val authRepository: AuthRepository,
    private val cloudinaryStorage: CloudinaryStorage,
    private val shareGenerator: ProductShareGenerator,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _shareUiState = MutableStateFlow(ShareUiState())
    val shareUiState: StateFlow<ShareUiState> = _shareUiState.asStateFlow()

    fun loadProducts(categoryId: String) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(categoryId)
            _uiState.value = _uiState.value.copy(categoryName = category?.name ?: "Products")

            productRepository.getProductsByCategory(categoryId).collect { products ->
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
            }
        }
    }

    fun loadProductDetail(productId: String) {
        viewModelScope.launch {
            val product = productRepository.getProductById(productId)
            _uiState.value = _uiState.value.copy(selectedProduct = product)
        }
    }

    fun addProduct(
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        imageUris: List<Uri>,
        context: Context
    ) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val productId = UUID.randomUUID().toString()

            val urls = mutableListOf<String>()
            val publicIds = mutableListOf<String>()
            for (uri in imageUris) {
                val result = cloudinaryStorage.uploadImage(uri, context, uid)
                if (result != null) {
                    urls.add(result.first)
                    publicIds.add(result.second)
                }
            }

            firestoreProductRepository.insertProduct(
                uid,
                Product(
                    id = productId,
                    categoryId = categoryId,
                    name = name,
                    description = description,
                    price = price,
                    imageUrls = urls,
                    cloudinaryPublicIds = publicIds
                )
            ).onFailure { e ->
                Log.e(TAG, "Failed to add product", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save product: ${e.message}")
            }
        }
    }

    fun addImagesToProduct(product: Product, imageUris: List<Uri>, context: Context) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch

            val newUrls = mutableListOf<String>()
            val newPublicIds = mutableListOf<String>()
            for (uri in imageUris) {
                val result = cloudinaryStorage.uploadImage(uri, context, uid)
                if (result != null) {
                    newUrls.add(result.first)
                    newPublicIds.add(result.second)
                }
            }

            if (newUrls.isNotEmpty()) {
                val updated = product.copy(
                    imageUrls = product.imageUrls + newUrls,
                    cloudinaryPublicIds = product.cloudinaryPublicIds + newPublicIds
                )
                firestoreProductRepository.updateProduct(uid, updated).onFailure { e ->
                    Log.e(TAG, "Failed to add images", e)
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to update product: ${e.message}")
                }
            }
        }
    }

    fun removeImageFromProduct(product: Product, imageUrl: String) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val index = product.imageUrls.indexOf(imageUrl)
            val newUrls = product.imageUrls.toMutableList().apply { remove(imageUrl) }
            val newPublicIds = product.cloudinaryPublicIds.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
            val updated = product.copy(
                imageUrls = newUrls,
                cloudinaryPublicIds = newPublicIds
            )
            firestoreProductRepository.updateProduct(uid, updated).onFailure { e ->
                Log.e(TAG, "Failed to remove image", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update product: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreProductRepository.deleteProduct(uid, product).onFailure { e ->
                Log.e(TAG, "Failed to delete product", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete product: ${e.message}")
            }
        }
    }

    fun deleteProducts(products: List<Product>) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            products.forEach { product ->
                firestoreProductRepository.deleteProduct(uid, product).onFailure { e ->
                    Log.e(TAG, "Failed to delete product: ${product.name}", e)
                }
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreProductRepository.updateProduct(uid, product).onFailure { e ->
                Log.e(TAG, "Failed to update product", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update product: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun shareProduct(context: Context, product: Product) {
        viewModelScope.launch {
            _shareUiState.value = _shareUiState.value.copy(isSharing = true)
            val prefs = getBusinessPrefs(context)
            val uri = shareGenerator.generateSingleProductPdf(
                product = product,
                businessName = prefs.name,
                businessEmail = prefs.email,
                businessPhone = prefs.phone,
                businessAddress = prefs.address
            )
            _shareUiState.value = _shareUiState.value.copy(shareUri = uri, isSharing = false)
        }
    }

    fun shareProducts(context: Context, products: List<Product>) {
        viewModelScope.launch {
            _shareUiState.value = _shareUiState.value.copy(isSharing = true)
            val prefs = getBusinessPrefs(context)
            val uri = shareGenerator.generateMultiProductPdf(
                products = products,
                businessName = prefs.name,
                businessEmail = prefs.email,
                businessPhone = prefs.phone,
                businessAddress = prefs.address
            )
            _shareUiState.value = _shareUiState.value.copy(shareUri = uri, isSharing = false)
        }
    }

    fun clearShareUri() {
        _shareUiState.value = _shareUiState.value.copy(shareUri = null)
    }

    private suspend fun getBusinessPrefs(context: Context): BusinessPrefs {
        val uid = authRepository.userId ?: return BusinessPrefs("", "", "", "")

        val prefs: SharedPreferences = context.getSharedPreferences(
            "mymanager_settings_$uid",
            Context.MODE_PRIVATE
        )

        val name = prefs.getString("business_name", "") ?: ""
        val email = prefs.getString("business_email", "") ?: ""
        val phone = prefs.getString("business_phone", "") ?: ""
        val address = prefs.getString("business_address", "") ?: ""

        if (name.isNotBlank() || email.isNotBlank() || phone.isNotBlank() || address.isNotBlank()) {
            return BusinessPrefs(name, email, phone, address)
        }

        return try {
            val profile = userProfileRepository.getProfile(uid)
            BusinessPrefs(
                name = profile?.businessName ?: "",
                email = profile?.businessEmail ?: "",
                phone = profile?.businessPhone ?: "",
                address = profile?.businessAddress ?: ""
            )
        } catch (e: Exception) {
            BusinessPrefs("", "", "", "")
        }
    }

    private data class BusinessPrefs(
        val name: String,
        val email: String,
        val phone: String,
        val address: String
    )

    companion object {
        private const val TAG = "ProductViewModel"
    }
}
