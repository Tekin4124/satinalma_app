/*
 * PurchaseRequest.kt
 * Satın alma talebini temsil eden domain model sınıfı.
 * Tüm talep bilgilerini (firma, ürün, durum, plaka, tarih, iptal, itiraz vb.) barındırır.
 */
package com.tekin.satinalma.domain.model

import java.util.UUID

/**
 * Satın alma talebi domain modeli
 * Yeni alanlar: iptal nedeni, itiraz bilgileri, yönlendirme ve yeni talep göstergesi
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
    // İptal bilgileri
    val cancellationReason: String? = null,
    val cancelledBy: String? = null,
    // Şoför itiraz bilgileri
    val objectionReason: String? = null,
    val hasObjection: Boolean = false,
    val objectionBy: String? = null,
    // Başka talebe yönlendirme
    val redirectedToRequestId: String? = null,
    // Yeni eklenen iş göstergesi (şoför ekranında okunmamış talepler)
    val isNew: Boolean = true,
    // Görülmüş kullanıcı ID'leri
    val seenByUserIds: Set<String> = emptySet()
)
