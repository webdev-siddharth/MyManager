package com.core2studio.mymanager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.core2studio.mymanager.data.local.entity.Order
import kotlinx.coroutines.flow.Flow

data class CategoryRevenue(
    val categoryName: String,
    val totalRevenue: Double,
    val orderCount: Int
)

data class MonthlyRevenue(
    val month: String,
    val revenue: Double
)

@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order)

    @Update
    suspend fun update(order: Order)

    @Delete
    suspend fun delete(order: Order)

    @Query("DELETE FROM orders")
    suspend fun deleteAll()

    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders ORDER BY date DESC")
    suspend fun getAllOrdersOnce(): List<Order>

    @Query("SELECT * FROM orders WHERE clientId = :clientId ORDER BY date DESC")
    fun getOrdersByClient(clientId: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY date DESC")
    fun getOrdersByStatus(status: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: String): Order?

    // --- Stats queries ---

    @Query("SELECT COALESCE(SUM(paidAmount), 0.0) FROM orders")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT COALESCE(SUM(amount - paidAmount), 0.0) FROM orders WHERE status != 'COMPLETED'")
    fun getPendingBalance(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM orders")
    fun getOrderCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT * FROM orders ORDER BY date DESC LIMIT :limit")
    fun getRecentOrders(limit: Int): Flow<List<Order>>

    // --- Aggregation queries ---

    @Query(
        """
        SELECT COALESCE(c.name, 'Uncategorized') AS categoryName, 
               COALESCE(SUM(t.paidAmount), 0.0) AS totalRevenue, 
               COUNT(t.id) AS orderCount
        FROM orders t
        LEFT JOIN products p ON t.productId = p.id
        LEFT JOIN categories c ON p.categoryId = c.id
        GROUP BY COALESCE(c.name, 'Uncategorized')
        ORDER BY totalRevenue DESC
        """
    )
    fun getRevenueByCategory(): Flow<List<CategoryRevenue>>

    @Query(
        """
        SELECT strftime('%Y-%m', date / 1000, 'unixepoch') AS month, 
               COALESCE(SUM(paidAmount), 0.0) AS revenue
        FROM orders
        GROUP BY month
        ORDER BY month DESC
        """
    )
    fun getMonthlyRevenue(): Flow<List<MonthlyRevenue>>
}
