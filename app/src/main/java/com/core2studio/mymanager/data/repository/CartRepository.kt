package com.core2studio.mymanager.data.repository

import com.core2studio.mymanager.data.local.dao.CartItemDao
import com.core2studio.mymanager.data.local.entity.CartItem
import kotlinx.coroutines.flow.Flow

class CartRepository(private val cartItemDao: CartItemDao) {

    fun getAllCartItems(): Flow<List<CartItem>> = cartItemDao.getAllCartItems()

    suspend fun getCartItemById(id: String): CartItem? = cartItemDao.getCartItemById(id)

    suspend fun getCartItemByProductId(productId: String): CartItem? = cartItemDao.getCartItemByProductId(productId)

    fun getCartTotal(): Flow<Double> = cartItemDao.getCartTotal()

    fun getCartCount(): Flow<Int> = cartItemDao.getCartCount()

    suspend fun insertCartItem(cartItem: CartItem) = cartItemDao.insert(cartItem)

    suspend fun updateCartItem(cartItem: CartItem) = cartItemDao.update(cartItem)

    suspend fun deleteCartItem(cartItem: CartItem) = cartItemDao.delete(cartItem)

    suspend fun clearCart() = cartItemDao.deleteAll()
}
