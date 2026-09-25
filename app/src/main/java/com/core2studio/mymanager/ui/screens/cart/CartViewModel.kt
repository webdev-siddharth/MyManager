package com.core2studio.mymanager.ui.screens.cart

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.firestore.FirestoreCartRepository
import com.core2studio.mymanager.data.firestore.FirestoreOrderRepository
import com.core2studio.mymanager.data.local.entity.CartItem
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.repository.CartRepository
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductShareGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val cartCount: Int = 0,
    val cartTotal: Double = 0.0,
    val isCheckingOut: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val errorMessage: String? = null
)

data class CartShareUiState(
    val shareUri: Uri? = null,
    val isSharing: Boolean = false
)

class CartViewModel(
    private val cartRepository: CartRepository,
    private val firestoreCartRepository: FirestoreCartRepository,
    private val orderRepository: OrderRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val clientRepository: ClientRepository,
    private val authRepository: AuthRepository,
    private val shareGenerator: ProductShareGenerator,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _shareUiState = MutableStateFlow(CartShareUiState())
    val shareUiState: StateFlow<CartShareUiState> = _shareUiState.asStateFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            launch {
                cartRepository.getAllCartItems().collect { items ->
                    _uiState.value = _uiState.value.copy(cartItems = items)
                }
            }
            launch {
                cartRepository.getCartCount().collect { count ->
                    _uiState.value = _uiState.value.copy(cartCount = count)
                }
            }
            launch {
                cartRepository.getCartTotal().collect { total ->
                    _uiState.value = _uiState.value.copy(cartTotal = total)
                }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val existingItem = cartRepository.getCartItemByProductId(product.id)
            if (existingItem != null) {
                val updatedItem = existingItem.copy(quantity = existingItem.quantity + quantity)
                firestoreCartRepository.updateCartItem(uid, updatedItem).onFailure { e ->
                    Log.e(TAG, "Failed to update cart item", e)
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to update cart: ${e.message}")
                }
            } else {
                val cartItem = CartItem(
                    productId = product.id,
                    productName = product.name,
                    productImage = product.imageUrls.firstOrNull() ?: "",
                    price = product.price,
                    quantity = quantity
                )
                firestoreCartRepository.insertCartItem(uid, cartItem).onFailure { e ->
                    Log.e(TAG, "Failed to add to cart", e)
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to add to cart: ${e.message}")
                }
            }
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        if (newQuantity < 1) {
            removeFromCart(cartItem)
            return
        }
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val updatedItem = cartItem.copy(quantity = newQuantity)
            firestoreCartRepository.updateCartItem(uid, updatedItem).onFailure { e ->
                Log.e(TAG, "Failed to update quantity", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update quantity: ${e.message}")
            }
        }
    }

    fun removeFromCart(cartItem: CartItem) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreCartRepository.deleteCartItem(uid, cartItem).onFailure { e ->
                Log.e(TAG, "Failed to remove from cart", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to remove item: ${e.message}")
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreCartRepository.clearCart(uid).onFailure { e ->
                Log.e(TAG, "Failed to clear cart", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to clear cart: ${e.message}")
            }
        }
    }

    fun checkout(
        clientId: String,
        notes: String,
        status: String,
        paidAmount: Double,
        paymentMethod: String = "",
        discountType: String = "AMOUNT",
        discountValue: Double = 0.0,
        referenceNumber: String = "",
        grandTotal: Double = 0.0,
        hasGstin: Boolean = false,
        gstRate: Int = 18,
        gstPricingMode: String = "INCLUSIVE",
        gstType: String = "CGST_SGST",
        customFields: Map<String, String>
    ) {
        // Guard synchronously, before the coroutine is dispatched: a check inside the
        // launched block lets a fast double-tap place the same order twice.
        if (_uiState.value.isCheckingOut) return
        _uiState.value = _uiState.value.copy(isCheckingOut = true)
        viewModelScope.launch {
            val uid = authRepository.userId
            if (uid == null) {
                _uiState.value = _uiState.value.copy(isCheckingOut = false, errorMessage = "You must be signed in to place an order")
                return@launch
            }
            val cartItems = _uiState.value.cartItems
            if (cartItems.isEmpty()) {
                _uiState.value = _uiState.value.copy(isCheckingOut = false, errorMessage = "Your cart is empty")
                return@launch
            }

            val amount = if (grandTotal > 0) grandTotal else cartItems.sumOf { it.price * it.quantity }
            val productNames = cartItems.joinToString(", ") { it.productName }

            val cartDetails = cartItems.map { item ->
                mapOf(
                    "productName" to item.productName,
                    "price" to item.price.toString(),
                    "quantity" to item.quantity.toString(),
                    "subtotal" to (item.price * item.quantity).toString(),
                    "hsnSacCode" to "",
                    "hsnSacType" to "HSN"
                )
            }
            val allCustomFields = customFields.toMutableMap()
            allCustomFields["cartItems"] = Json.encodeToString(cartDetails)
            if (discountValue > 0) {
                allCustomFields["discountType"] = discountType
                allCustomFields["discountValue"] = discountValue.toString()
            }
            if (referenceNumber.isNotBlank()) {
                allCustomFields["referenceNumber"] = referenceNumber
            }
            if (hasGstin) {
                allCustomFields["gstRate"] = gstRate.toString()
                allCustomFields["gstType"] = gstType
                allCustomFields["gstPricingMode"] = gstPricingMode
            }

            val order = Order(
                clientId = clientId,
                productName = if (cartItems.size == 1) cartItems.first().productName else productNames,
                quantity = cartItems.sumOf { it.quantity },
                amount = amount,
                paidAmount = paidAmount,
                status = status,
                paymentMethod = paymentMethod,
                customFields = Json.encodeToString(allCustomFields),
                notes = notes
            )

            firestoreOrderRepository.insertOrder(uid, order).onSuccess {
                var clearCartError: String? = null
                firestoreCartRepository.clearCart(uid).onFailure { e ->
                    Log.e(TAG, "Failed to clear cart after checkout", e)
                    clearCartError = "Order saved, but the cart could not be cleared: ${e.message}"
                }
                _uiState.value = _uiState.value.copy(
                    isCheckingOut = false,
                    checkoutSuccess = true,
                    errorMessage = clearCartError
                )
            }.onFailure { e ->
                Log.e(TAG, "Failed to create order from cart", e)
                _uiState.value = _uiState.value.copy(isCheckingOut = false, errorMessage = "Failed to place order: ${e.message}")
            }
        }
    }

    fun resetCheckoutSuccess() {
        _uiState.value = _uiState.value.copy(checkoutSuccess = false, isCheckingOut = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun shareCart(context: Context) {
        viewModelScope.launch {
            val items = _uiState.value.cartItems
            if (items.isEmpty()) return@launch
            _shareUiState.value = _shareUiState.value.copy(isSharing = true)
            val prefs = getBusinessPrefs(context)
            val uri = shareGenerator.generateCartPdf(
                cartItems = items,
                cartTotal = _uiState.value.cartTotal,
                businessName = prefs.name,
                businessEmail = prefs.email,
                businessPhone = prefs.phone,
                businessAddress = prefs.address,
                businessWebsite = prefs.website
            )
            _shareUiState.value = _shareUiState.value.copy(shareUri = uri, isSharing = false)
        }
    }

    fun clearShareUri() {
        _shareUiState.value = _shareUiState.value.copy(shareUri = null)
    }

    private suspend fun getBusinessPrefs(context: Context): BusinessPrefs {
        val uid = authRepository.userId ?: return BusinessPrefs("", "", "", "", "")

        val prefs: SharedPreferences = context.getSharedPreferences(
            "mymanager_settings_$uid",
            Context.MODE_PRIVATE
        )

        val name = prefs.getString("business_name", "") ?: ""
        val email = prefs.getString("business_email", "") ?: ""
        val phone = prefs.getString("business_phone", "") ?: ""
        val address = prefs.getString("business_address", "") ?: ""
        val website = prefs.getString("business_website", "") ?: ""

        if (name.isNotBlank() || email.isNotBlank() || phone.isNotBlank() || address.isNotBlank()) {
            return BusinessPrefs(name, email, phone, address, website)
        }

        return try {
            val profile = userProfileRepository.getProfile(uid)
            BusinessPrefs(
                name = profile?.businessName ?: "",
                email = profile?.businessEmail ?: "",
                phone = profile?.businessPhone ?: "",
                address = profile?.businessAddress ?: "",
                website = profile?.website ?: ""
            )
        } catch (e: Exception) {
            BusinessPrefs("", "", "", "", "")
        }
    }

    private data class BusinessPrefs(
        val name: String,
        val email: String,
        val phone: String,
        val address: String,
        val website: String
    )

    companion object {
        private const val TAG = "CartViewModel"
    }
}
