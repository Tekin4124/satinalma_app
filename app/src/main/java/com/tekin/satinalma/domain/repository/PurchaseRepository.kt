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

    /** Şoförü talebe atar (plaka dahil) */
    suspend fun assignDriver(requestId: String, driverId: String, licensePlate: String)

    /** Talebi iptal eder */
    suspend fun cancelRequest(requestId: String, reason: String?, cancelledBy: String)

    /** Şoförden itiraz gönderir */
    suspend fun submitObjection(requestId: String, driverId: String, reason: String)

    /** Tüm şoförleri döner */
    suspend fun getDrivers(): List<com.tekin.satinalma.domain.model.User>
}
