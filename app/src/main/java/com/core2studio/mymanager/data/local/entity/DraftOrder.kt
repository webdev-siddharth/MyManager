package com.core2studio.mymanager.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "draft_orders",
    indices = [Index("userId")]
)
data class DraftOrder(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val itemsJson: String = "[]",
    val discountType: String = "AMOUNT",
    val discountValue: Double = 0.0,
    val gstPricingMode: String = "INCLUSIVE",
    val gstRate: Int = 18,
    val gstType: String = "CGST_SGST",
    val hasGstin: Boolean = false,
    val paidAmount: Double = 0.0,
    val status: String = "PENDING",
    val paymentMethod: String = "Cash-in-hand",
    val referenceNumber: String = "",
    val notes: String = "",
    val customFieldsJson: String = "{}",
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class OrderItemDraft(
    val id: String = java.util.UUID.randomUUID().toString(),
    val productId: String? = null,
    val productName: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val imageUrl: String = "",
    val hsnSacCode: String = "",
    val hsnSacType: String = "HSN"
) {
    val subtotal: Double
        get() = quantity * unitPrice
}