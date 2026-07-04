package com.core2studio.mymanager.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.firestore.SyncManager
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val totalRevenue: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val orderCount: Int = 0,
    val completedCount: Int = 0,
    val recentProducts: List<Product> = emptyList(),
    val clients: Map<String, Client> = emptyMap(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val orderRepository: OrderRepository,
    private val clientRepository: ClientRepository,
    private val productRepository: ProductRepository,
    private val syncManager: SyncManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            launch {
                orderRepository.getTotalRevenue().collect { revenue ->
                    _uiState.value = _uiState.value.copy(totalRevenue = revenue ?: 0.0)
                }
            }
            launch {
                orderRepository.getPendingBalance().collect { pending ->
                    _uiState.value = _uiState.value.copy(pendingBalance = pending ?: 0.0)
                }
            }
            launch {
                orderRepository.getOrderCount().collect { count ->
                    _uiState.value = _uiState.value.copy(orderCount = count)
                }
            }
            launch {
                orderRepository.getCompletedCount().collect { count ->
                    _uiState.value = _uiState.value.copy(completedCount = count)
                }
            }
            launch {
                productRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(recentProducts = products)
                }
            }
            launch {
                clientRepository.getAllClients().collect { clientList ->
                    _uiState.value = _uiState.value.copy(
                        clients = clientList.associateBy { it.id }
                    )
                }
            }
        }
    }

    fun refresh() {
        val uid = authRepository.userId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            syncManager.syncAllFromFirestore(uid)
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        errorMessage = "Sync failed. Showing last available data."
                    )
                }
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isRefreshing = false)
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
