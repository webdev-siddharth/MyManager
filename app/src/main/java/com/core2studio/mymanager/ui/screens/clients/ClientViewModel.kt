package com.core2studio.mymanager.ui.screens.clients

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.firestore.FirestoreClientRepository
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.InvoiceGenerator
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ClientUiState(
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val filteredClients: List<Client> = emptyList(),
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val showEditDialogClient: Client? = null,
    val selectedClient: Client? = null,
    val clientTransactions: List<Order> = emptyList(),
    val products: Map<String, Product> = emptyMap(),
    val invoiceMessage: String? = null,
    val invoiceUri: Uri? = null,
    val errorMessage: String? = null
)

@OptIn(FlowPreview::class)
class ClientViewModel(
    private val clientRepository: ClientRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val invoiceGenerator: InvoiceGenerator,
    private val firestoreClientRepository: FirestoreClientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientUiState())
    val uiState: StateFlow<ClientUiState> = _uiState.asStateFlow()

    private var businessName: String = "MyManager"
    private var businessEmail: String = ""
    private var businessPhone: String = ""
    private var businessAddress: String = ""
    private var businessWebsite: String = ""

    private var loadDetailJob: Job? = null

    init {
        loadClients()
        loadProducts()
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

    private fun loadClients() {
        viewModelScope.launch {
            clientRepository.getAllClients().collect { clients ->
                _uiState.value = _uiState.value.copy(
                    clients = clients,
                    filteredClients = if (_uiState.value.searchQuery.isBlank()) clients else _uiState.value.filteredClients
                )
            }
        }
    }

    private fun loadProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { products ->
                _uiState.value = _uiState.value.copy(
                    products = products.associateBy { it.id }
                )
            }
        }
    }

    fun loadClientDetail(clientId: String) {
        loadDetailJob?.cancel()
        loadDetailJob = viewModelScope.launch {
            val client = clientRepository.getClientById(clientId)
            _uiState.value = _uiState.value.copy(selectedClient = client)

            if (client != null) {
                orderRepository.getOrdersByClient(clientId).collect { orders ->
                    _uiState.value = _uiState.value.copy(clientTransactions = orders)
                }
            }
        }
    }

    fun showAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = true)
    }

    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
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
            dismissDialog()
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            firestoreClientRepository.deleteClient(uid, client).onFailure { e ->
                Log.e(TAG, "Failed to delete client", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to delete client: ${e.message}")
            }
        }
    }

    fun showEditDialog(client: Client) {
        _uiState.value = _uiState.value.copy(showEditDialog = true, showEditDialogClient = client)
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(showEditDialog = false, showEditDialogClient = null)
    }

    fun updateClient(clientId: String, name: String, phone: String, email: String, address: String) {
        viewModelScope.launch {
            val uid = authRepository.userId ?: return@launch
            val current = _uiState.value.showEditDialogClient ?: return@launch
            val updated = current.copy(name = name, phone = phone, email = email, address = address)
            firestoreClientRepository.updateClient(uid, updated).onFailure { e ->
                Log.e(TAG, "Failed to update client", e)
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to update client: ${e.message}")
            }
            dismissEditDialog()
            loadClientDetail(clientId)
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
            val client = _uiState.value.selectedClient ?: return@launch
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
        val q = query.lowercase().trim()
        val allClients = _uiState.value.clients

        val filtered = if (q.isBlank()) allClients
        else allClients.filter { client ->
            client.name.lowercase().contains(q) ||
            client.phone.contains(q) ||
            client.email.lowercase().contains(q) ||
            client.address.lowercase().contains(q)
        }

        _uiState.value = _uiState.value.copy(filteredClients = filtered)
    }

    companion object {
        private const val TAG = "ClientViewModel"
    }
}
