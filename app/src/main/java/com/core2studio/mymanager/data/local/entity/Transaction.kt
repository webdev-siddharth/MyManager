package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey val id: String = "",
    val clientId: String = "",
    val productId: String? = null,
    val productName: String = "",
    val amount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING",
    val customFields: String = "{}",
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
