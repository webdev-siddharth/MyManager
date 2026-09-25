package com.core2studio.mymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.core2studio.mymanager.data.firestore.FirestoreUserRepository
import com.core2studio.mymanager.data.local.MyManagerDatabase
import com.core2studio.mymanager.ui.screens.auth.AuthViewModel
import com.core2studio.mymanager.ui.screens.categories.CategoryViewModel
import com.core2studio.mymanager.ui.screens.clients.ClientViewModel
import com.core2studio.mymanager.ui.screens.dashboard.DashboardViewModel
import com.core2studio.mymanager.ui.screens.products.ProductViewModel
import com.core2studio.mymanager.ui.screens.settings.SettingsViewModel
import com.core2studio.mymanager.ui.screens.orders.CreateOrderViewModel
import com.core2studio.mymanager.ui.screens.orders.OrderViewModel
import com.core2studio.mymanager.ui.screens.cart.CartViewModel

class MyManagerViewModelFactory(
    private val app: MyManagerApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.auth.AuthViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.auth.AuthViewModel(
                    authRepository = app.authRepository,
                    userProfileRepository = app.userProfileRepository,
                    syncManager = app.syncManager
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.dashboard.DashboardViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.dashboard.DashboardViewModel(
                    orderRepository = app.orderRepository,
                    clientRepository = app.clientRepository,
                    productRepository = app.productRepository,
                    syncManager = app.syncManager,
                    authRepository = app.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.categories.CategoryViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.categories.CategoryViewModel(
                    categoryRepository = app.categoryRepository,
                    productRepository = app.productRepository,
                    firestoreCategoryRepository = app.firestoreCategoryRepository,
                    authRepository = app.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.products.ProductViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.products.ProductViewModel(
                    productRepository = app.productRepository,
                    categoryRepository = app.categoryRepository,
                    firestoreProductRepository = app.firestoreProductRepository,
                    authRepository = app.authRepository,
                    cloudinaryStorage = app.cloudinaryStorage,
                    shareGenerator = app.productShareGenerator,
                    userProfileRepository = app.userProfileRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.orders.OrderViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.orders.OrderViewModel(
                    orderRepository = app.orderRepository,
                    clientRepository = app.clientRepository,
                    productRepository = app.productRepository,
                    firestoreOrderRepository = app.firestoreOrderRepository,
                    firestoreClientRepository = app.firestoreClientRepository,
                    authRepository = app.authRepository,
                    invoiceGenerator = app.invoiceGenerator
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.orders.CreateOrderViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.orders.CreateOrderViewModel(
                    orderRepository = app.orderRepository,
                    firestoreOrderRepository = app.firestoreOrderRepository,
                    clientRepository = app.clientRepository,
                    firestoreClientRepository = app.firestoreClientRepository,
                    productRepository = app.productRepository,
                    draftOrderDao = app.database.draftOrderDao(),
                    authRepository = app.authRepository,
                    settingsRepository = app.settingsRepository,
                    applicationScope = app.applicationScope
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.clients.ClientViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.clients.ClientViewModel(
                    clientRepository = app.clientRepository,
                    orderRepository = app.orderRepository,
                    productRepository = app.productRepository,
                    invoiceGenerator = app.invoiceGenerator,
                    firestoreClientRepository = app.firestoreClientRepository,
                    authRepository = app.authRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.settings.SettingsViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.settings.SettingsViewModel(
                    authRepository = app.authRepository,
                    userProfileRepository = app.userProfileRepository,
                    firestoreUserRepository = app.firestoreUserRepository,
                    database = app.database,
                    cloudinaryStorage = app.cloudinaryStorage,
                    context = app
                ) as T
            }
            modelClass.isAssignableFrom(com.core2studio.mymanager.ui.screens.cart.CartViewModel::class.java) -> {
                com.core2studio.mymanager.ui.screens.cart.CartViewModel(
                    cartRepository = app.cartRepository,
                    firestoreCartRepository = app.firestoreCartRepository,
                    orderRepository = app.orderRepository,
                    firestoreOrderRepository = app.firestoreOrderRepository,
                    clientRepository = app.clientRepository,
                    authRepository = app.authRepository,
                    shareGenerator = app.productShareGenerator,
                    userProfileRepository = app.userProfileRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
