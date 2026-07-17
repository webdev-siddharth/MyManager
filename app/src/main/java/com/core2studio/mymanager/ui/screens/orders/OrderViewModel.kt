package com.core2studio.mymanager.ui.screens.orders

import android.net.Uri
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
import com.core2studio.mymanager.data.repository.InvoiceGenerator
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

data class OrderUiState(
    val orders: List<Order> = emptyList(),
    val searchQuery: String = "",
    val filteredOrders: List<Order> = emptyList(),
    val clients: Map<String, Client> = emptyMap(),
    val products: Map<String, Product> = emptyMap(),
    val allClients: List<Client> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val filterStatus: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val invoiceUri: Uri? = null,
    val invoiceMessage: String? = null
)

@OptIn(FlowPreview::class)
class OrderViewModel(
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val firestoreOrderRepository: FirestoreOrderRepository,
    private val firestoreClientRepository: FirestoreClientRepository,
    private val authRepository: AuthRepository,
    private val invoiceGenerator: InvoiceGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private var businessName = ""
    private var businessEmail = ""
    private var businessPhone = ""
    private var businessAddress = ""
    private var businessWebsite = ""

    init {
        loadData()
        setupSearch()
    }

    private fun setupSearch() {
        viewModelScope.launch {
            _uiState
                .map { it.searchQuery }
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            launch {
                orderRepository.getAllOrders().collect { orders ->
                    _uiState.value = _uiState.value.copy(orders = orders)
                    performSearch(_uiState.value.searchQuery)
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
        performSearch(_uiState.value.searchQuery)
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
        quantity: Int,
        unitPrice: Double,
        amount: Double,
        paidAmount: Double,
        status: String,
        paymentMethod: String,
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
                    quantity = quantity,
                    unitPrice = unitPrice,
                    amount = amount,
                    paidAmount = paidAmount,
                    status = status,
                    paymentMethod = paymentMethod,
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

    fun setBusinessInfo(name: String, email: String, phone: String, address: String, website: String = "") {
        businessName = name
        businessEmail = email
        businessPhone = phone
        businessAddress = address
        businessWebsite = website
    }

    fun generateInvoice(order: Order) {
        viewModelScope.launch {
            val client = _uiState.value.clients[order.clientId] ?: run {
                _uiState.value = _uiState.value.copy(invoiceMessage = "Client not found")
                return@launch
            }
            val productName = order.productName.ifBlank { null }

            val result = withContext(Dispatchers.IO) {
                invoiceGenerator.generateInvoice(
                    transaction = order,
                    client = client,
                    productName = productName,
                    businessName = businessName,
                    businessEmail = businessEmail,
                    businessPhone = businessPhone,
                    businessAddress = businessAddress,
                    businessWebsite = businessWebsite
                )
            }

            _uiState.value = _uiState.value.copy(
                invoiceMessage = if (result != null) {
                    "Invoice saved to Downloads: ${result.fileName}"
                } else {
                    "Failed to generate invoice"
                },
                invoiceUri = result?.uri
            )
        }
    }

    fun clearInvoiceUri() {
        _uiState.value = _uiState.value.copy(invoiceUri = null)
    }

    fun clearInvoiceMessage() {
        _uiState.value = _uiState.value.copy(invoiceMessage = null)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun performSearch(query: String) {
        val state = _uiState.value
        var result = state.orders

        // Apply status filter
        if (state.filterStatus != null) {
            result = result.filter { it.status == state.filterStatus }
        }

        // Apply search filter
        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            result = result.filter { order ->
                order.productName.lowercase().contains(q) ||
                order.orderId.lowercase().contains(q) ||
                order.notes.lowercase().contains(q) ||
                state.clients[order.clientId]?.name?.lowercase()?.contains(q) == true
            }
        }

        _uiState.value = _uiState.value.copy(filteredOrders = result)
    }

    companion object {
        private const val TAG = "OrderViewModel"
    }
}
