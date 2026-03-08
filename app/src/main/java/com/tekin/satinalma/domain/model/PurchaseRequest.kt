/*
 * PurchaseRequest.kt
 * Satın alma talebini temsil eden domain model sınıfı.
 * Tüm talep bilgilerini (firma, ürün, durum, plaka, tarih vb.) barındırır.
 */
package com.tekin.satinalma.domain.model

import java.util.UUID

/**
 * Satın alma talebi domain modeli
 */
data class PurchaseRequest(
    val id: String = UUID.randomUUID().toString(),
    val requestNumber: String = "",
    val itemToPurchase: String = "",
    val companyName: String = "",
    val companyAddress: String = "",
    val contactNumber: String = "",
    val productDimensions: String = "",
    val productWeight: String = "",
    val urgencyLevel: UrgencyLevel = UrgencyLevel.NORMAL,
    val purchaseDate: String = "",
    val notes: String = "",
    val licensePlate: String = "",
    val materialStatus: MaterialStatus = MaterialStatus.PENDING,
    val assignedDriverId: String? = null,
    val assignedPurchasingStaffId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val cancellationReason: String? = null,    // İptal nedeni
    val cancelledBy: String? = null,           // Kim iptal etti
    val objectionReason: String? = null,       // Şoför itiraz nedeni
    val hasObjection: Boolean = false,         // İtiraz var mı
    val objectionBy: String? = null            // İtiraz eden şoför ID
)
