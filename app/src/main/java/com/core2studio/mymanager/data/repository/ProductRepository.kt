package com.core2studio.mymanager.data.repository

import com.core2studio.mymanager.data.local.dao.ProductDao
import com.core2studio.mymanager.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId)

    suspend fun getProductById(id: String): Product? = productDao.getProductById(id)

    suspend fun insertProduct(product: Product) = productDao.insert(product)

    suspend fun updateProduct(product: Product) = productDao.update(product)

    suspend fun deleteProduct(product: Product) = productDao.delete(product)
}
