package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String = "",
    val name: String,
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
