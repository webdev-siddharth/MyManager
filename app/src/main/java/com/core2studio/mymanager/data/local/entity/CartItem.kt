package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val id: String = "",
    val productId: String,
    val productName: String,
    val productImage: String = "",
    val price: Double,
    val quantity: Int = 1,
    val addedAt: Long = System.currentTimeMillis()
)
