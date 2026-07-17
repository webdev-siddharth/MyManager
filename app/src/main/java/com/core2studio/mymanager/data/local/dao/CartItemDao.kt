package com.core2studio.mymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.core2studio.mymanager.data.local.entity.CartItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartItem: CartItem)

    @Update
    suspend fun update(cartItem: CartItem)

    @Delete
    suspend fun delete(cartItem: CartItem)

    @Query("DELETE FROM cart_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItem>>

    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    suspend fun getAllOnce(): List<CartItem>

    @Query("SELECT * FROM cart_items WHERE id = :id")
    suspend fun getCartItemById(id: String): CartItem?

    @Query("SELECT * FROM cart_items WHERE productId = :productId LIMIT 1")
    suspend fun getCartItemByProductId(productId: String): CartItem?

    @Query("SELECT COALESCE(SUM(price * quantity), 0.0) FROM cart_items")
    fun getCartTotal(): Flow<Double>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartCount(): Flow<Int>
}
