package com.core2studio.mymanager

import android.app.Application
import android.util.Log
import com.core2studio.mymanager.data.auth.AuthRepository
import com.core2studio.mymanager.data.auth.UserProfileRepository
import com.core2studio.mymanager.data.firestore.FirestoreCategoryRepository
import com.core2studio.mymanager.data.firestore.FirestoreClientRepository
import com.core2studio.mymanager.data.firestore.FirestoreCartRepository
import com.core2studio.mymanager.data.firestore.FirestoreOrderRepository
import com.core2studio.mymanager.data.firestore.FirestoreProductRepository
import com.core2studio.mymanager.data.firestore.SyncManager
import com.core2studio.mymanager.data.storage.CloudinaryStorage
import com.core2studio.mymanager.data.local.MyManagerDatabase
import com.core2studio.mymanager.data.repository.CategoryRepository
import com.core2studio.mymanager.data.repository.ClientRepository
import com.core2studio.mymanager.data.repository.InvoiceGenerator
import com.core2studio.mymanager.data.repository.ProductRepository
import com.core2studio.mymanager.data.repository.ProductShareGenerator
import com.core2studio.mymanager.data.repository.OrderRepository
import com.core2studio.mymanager.data.repository.CartRepository
import com.google.firebase.FirebaseApp

class MyManagerApplication : Application() {

    val database: com.core2studio.mymanager.data.local.MyManagerDatabase by lazy {
        com.core2studio.mymanager.data.local.MyManagerDatabase.getInstance(this)
    }

    val authRepository: com.core2studio.mymanager.data.auth.AuthRepository by lazy {
        com.core2studio.mymanager.data.auth.AuthRepository()
    }

    val userProfileRepository: com.core2studio.mymanager.data.auth.UserProfileRepository by lazy {
        com.core2studio.mymanager.data.auth.UserProfileRepository()
    }

    val categoryRepository: com.core2studio.mymanager.data.repository.CategoryRepository by lazy {
        com.core2studio.mymanager.data.repository.CategoryRepository(database.categoryDao())
    }

    val productRepository: com.core2studio.mymanager.data.repository.ProductRepository by lazy {
        com.core2studio.mymanager.data.repository.ProductRepository(database.productDao())
    }

    val clientRepository: com.core2studio.mymanager.data.repository.ClientRepository by lazy {
        com.core2studio.mymanager.data.repository.ClientRepository(database.clientDao())
    }

    val orderRepository: com.core2studio.mymanager.data.repository.OrderRepository by lazy {
        com.core2studio.mymanager.data.repository.OrderRepository(database.orderDao())
    }

    val cartRepository: com.core2studio.mymanager.data.repository.CartRepository by lazy {
        com.core2studio.mymanager.data.repository.CartRepository(database.cartItemDao())
    }

    val firestoreCategoryRepository: com.core2studio.mymanager.data.firestore.FirestoreCategoryRepository by lazy {
        com.core2studio.mymanager.data.firestore.FirestoreCategoryRepository(
            database.categoryDao(),
            database.productDao()
        )
    }

    val firestoreClientRepository: com.core2studio.mymanager.data.firestore.FirestoreClientRepository by lazy {
        com.core2studio.mymanager.data.firestore.FirestoreClientRepository(
            database.clientDao()
        )
    }

    val firestoreProductRepository: com.core2studio.mymanager.data.firestore.FirestoreProductRepository by lazy {
        com.core2studio.mymanager.data.firestore.FirestoreProductRepository(
            database.productDao()
        )
    }

    val firestoreOrderRepository: com.core2studio.mymanager.data.firestore.FirestoreOrderRepository by lazy {
        com.core2studio.mymanager.data.firestore.FirestoreOrderRepository(
            database.orderDao()
        )
    }

    val firestoreCartRepository: com.core2studio.mymanager.data.firestore.FirestoreCartRepository by lazy {
        com.core2studio.mymanager.data.firestore.FirestoreCartRepository(
            database.cartItemDao()
        )
    }

    val syncManager: com.core2studio.mymanager.data.firestore.SyncManager by lazy {
        com.core2studio.mymanager.data.firestore.SyncManager(
            database = database,
            firestoreCategoryRepository = firestoreCategoryRepository,
            firestoreClientRepository = firestoreClientRepository,
            firestoreProductRepository = firestoreProductRepository,
            firestoreOrderRepository = firestoreOrderRepository,
            firestoreCartRepository = firestoreCartRepository
        )
    }

    val invoiceGenerator: com.core2studio.mymanager.data.repository.InvoiceGenerator by lazy {
        com.core2studio.mymanager.data.repository.InvoiceGenerator(this)
    }

    val productShareGenerator: com.core2studio.mymanager.data.repository.ProductShareGenerator by lazy {
        com.core2studio.mymanager.data.repository.ProductShareGenerator(this)
    }

    val cloudinaryStorage: com.core2studio.mymanager.data.storage.CloudinaryStorage by lazy {
        com.core2studio.mymanager.data.storage.CloudinaryStorage(
            cloudName = "dry3uxhvm",
            uploadPreset = "business_manager_products"
        )
    }

    override fun onCreate() {
        super.onCreate()
        try {
            FirebaseApp.initializeApp(this)
            Log.i("MyManager", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("MyManager", "Firebase initialization failed", e)
        }
    }
}
