package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val id: String = "",
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
