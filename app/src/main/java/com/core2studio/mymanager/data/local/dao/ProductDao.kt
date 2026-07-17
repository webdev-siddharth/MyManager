package com.core2studio.mymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.core2studio.mymanager.data.local.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products")
    suspend fun deleteAll()

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllOnce(): List<Product>

    @Query("SELECT * FROM products ORDER BY createdAt DESC LIMIT 4")
    fun getRecentProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): Product?

    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    suspend fun getProductsByCategoryOnce(categoryId: String): List<Product>
}
