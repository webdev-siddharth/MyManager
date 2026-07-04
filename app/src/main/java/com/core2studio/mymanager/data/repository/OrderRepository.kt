package com.core2studio.mymanager.data.repository

import com.core2studio.mymanager.data.local.dao.CategoryRevenue
import com.core2studio.mymanager.data.local.dao.MonthlyRevenue
import com.core2studio.mymanager.data.local.dao.OrderDao
import com.core2studio.mymanager.data.local.entity.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

class OrderRepository(private val orderDao: OrderDao) {

    // --- CRUD ---

    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()

    fun getOrdersByClient(clientId: String): Flow<List<Order>> =
        orderDao.getOrdersByClient(clientId)

    fun getOrdersByStatus(status: String): Flow<List<Order>> =
        orderDao.getOrdersByStatus(status)

    suspend fun getOrderById(id: String): Order? =
        orderDao.getOrderById(id)

    suspend fun insertOrder(order: Order) =
        orderDao.insert(order)

    suspend fun updateOrder(order: Order) =
        orderDao.update(order)

    suspend fun deleteOrder(order: Order) =
        orderDao.delete(order)

    // --- Stats ---

    fun getTotalRevenue(): Flow<Double?> = orderDao.getTotalRevenue()

    fun getPendingBalance(): Flow<Double?> = orderDao.getPendingBalance()

    fun getOrderCount(): Flow<Int> = orderDao.getOrderCount()

    fun getCompletedCount(): Flow<Int> = orderDao.getCompletedCount()

    fun getRecentOrders(limit: Int = 10): Flow<List<Order>> =
        orderDao.getRecentOrders(limit)

    // --- Aggregations ---

    fun getRevenueByCategory(): Flow<List<CategoryRevenue>> =
        orderDao.getRevenueByCategory()

    fun getMonthlyRevenue(): Flow<List<MonthlyRevenue>> =
        orderDao.getMonthlyRevenue()

    // --- Custom fields helpers ---

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parseCustomFields(jsonString: String): Map<String, String> {
            return try {
                json.decodeFromString<Map<String, String>>(jsonString)
            } catch (_: Exception) {
                emptyMap()
            }
        }

        fun serializeCustomFields(fields: Map<String, String>): String {
            return json.encodeToString(
                kotlinx.serialization.serializer<Map<String, String>>(),
                fields
            )
        }
    }
}
