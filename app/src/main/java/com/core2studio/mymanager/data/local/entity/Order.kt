package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String = "",
    val orderId: String = "",
    val clientId: String = "",
    val productId: String? = null,
    val productName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING",
    val paymentMethod: String = "",
    val customFields: String = "{}",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val lastPaymentDate: Long = 0L
)
