package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey val id: String = "",
    val categoryId: String = "",
    val name: String,
    val description: String = "",
    val price: Double = 0.0,
    val imageUrls: List<String> = emptyList(),
    val cloudinaryPublicIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
