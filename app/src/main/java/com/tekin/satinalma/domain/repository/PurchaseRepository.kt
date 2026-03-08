/*
 * PurchaseRepository.kt
 * Satın alma talepleri için veri erişim sözleşmesini (interface) tanımlar.
 * Domain katmanı bu arayüze bağımlıdır; implementasyon data katmanında yapılır.
 */
package com.tekin.satinalma.domain.repository

import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.MaterialStatus
import kotlinx.coroutines.flow.Flow

/**
 * Satın alma talebi veri erişim arayüzü
 */
interface PurchaseRepository {

    /** Tüm talepleri gerçek zamanlı akış olarak döner */
    fun getAllRequests(): Flow<List<PurchaseRequest>>

    /** Belirtilen ID'ye sahip talebi döner */
    suspend fun getRequestById(id: String): PurchaseRequest?

    /** Yeni talep oluşturur */
    suspend fun createRequest(request: PurchaseRequest)

    /** Mevcut talebi günceller */
    suspend fun updateRequest(request: PurchaseRequest)

    /** Talep durumunu günceller */
    suspend fun updateStatus(requestId: String, status: MaterialStatus)

    /** Şoförü talebe atar */
    suspend fun assignDriver(requestId: String, driverId: String)

    /** Plaka bilgisini günceller */
    suspend fun updateLicensePlate(requestId: String, plate: String)
}
