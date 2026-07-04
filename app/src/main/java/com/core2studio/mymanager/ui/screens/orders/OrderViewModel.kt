package com.core2studio.mymanager.ui.screens.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.firestore.FirestoreClientRepository
import com.core2studio.mymanager.data.firestore.FirestoreOrderRepository
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val clients: Map<String, Client> = emptyMap(),
    val products: Map<String, Product> = emptyMap(),
    val allClients: List<Client> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val filterStatus: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val firestoreClientRepository: FirestoreClientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                orderRepository.getAllOrders().collect { orders ->
                    _uiState.value = _uiState.value.copy(
                        orders = applyFilter(orders, _uiState.value.filterStatus)
                    )
                }
            }
            launch {
                clientRepository.getAllClients().collect { clients ->
                    _uiState.value = _uiState.value.copy(
                        allClients = clients,
                        clients = clients.associateBy { it.id }
                    )
                }
            }
            launch {
                productRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        allProducts = products,
                        products = products.associateBy { it.id }
                    )
                }
            }
        }
    }

    fun setFilter(status: String?) {
        _uiState.value = _uiState.value.copy(filterStatus = status)
        viewModelScope.launch {
            orderRepository.getAllOrders().collect { orders ->
                _uiState.value = _uiState.value.copy(
                    orders = applyFilter(orders, status)
                )
            }
        }
    }

    private fun applyFilter(orders: List<Order>, status: String?): List<Order> {
        return if (status == null) orders
        else orders.filter { it.status == status }
    }

    fun addClient(name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreClientRepository.insertClient(
                uid,
                Client(name = name, phone = phone, email = email, address = address)
            ).onFailure { e ->
                Log.e(TAG, "Failed to add client", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save client: ${e.message}")
            }
        }
    }

    fun addOrder(
        clientId: String,
        productName: String,
        amount: Double,
        paidAmount: Double,
        status: String,
        customFields: Map<String, String>,
        notes: String
    ) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val customFieldsJson = try {
                Json.encodeToString(customFields)
            } catch (e: Exception) {
                "{}"
            }

            firestoreOrderRepository.insertOrder(
                uid,
                Order(
                    clientId = clientId,
                    productName = productName,
                    amount = amount,
                    paidAmount = paidAmount,
                    status = status,
                    customFields = customFieldsJson,
                    notes = notes
                )
            ).onFailure { e ->
                Log.e(TAG, "Failed to add order", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save order: ${e.message}")
            }
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreOrderRepository.deleteOrder(uid, order).onFailure { e ->
                Log.e(TAG, "Failed to delete order", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete order: ${e.message}")
            }
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreOrderRepository.updateOrder(uid, order).onFailure { e ->
                Log.e(TAG, "Failed to update order", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update order: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        private const val TAG = "OrderViewModel"
    }
}
