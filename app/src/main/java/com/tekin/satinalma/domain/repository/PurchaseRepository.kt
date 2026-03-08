/*
 * PurchaseRepository.kt
 * Satın alma talepleri için veri erişim sözleşmesini (interface) tanımlar.
 * Domain katmanı bu arayüze bağımlıdır; implementasyon data katmanında yapılır.
 */
package com.tekin.satinalma.domain.repository

import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.User
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

    /** Şoförü talebe atar ve plaka bilgisini günceller */
    suspend fun assignDriver(requestId: String, driverId: String, licensePlate: String)

    /** Talebi iptal eder */
    suspend fun cancelRequest(requestId: String, reason: String?, cancelledBy: String)

    /** Şoför itirazı gönderir */
    suspend fun submitObjection(requestId: String, driverId: String, reason: String)

    /** Talebi başka bir talebe yönlendirir */
    suspend fun redirectRequest(requestId: String, targetRequestId: String)

    /** Talebi okundu olarak işaretler */
    suspend fun markAsSeen(requestId: String, userId: String)

    /** Mevcut şoförlerin listesini döner */
    fun getDrivers(): Flow<List<User>>

    /** Kullanıcıya yönelik bildirimleri döner */
    fun getNotifications(userId: String): Flow<List<AppNotification>>

    /** Bildirimi okundu olarak işaretler */
    suspend fun markNotificationRead(notificationId: String)

    /** Sonraki talep numarasını üretir (TLP-YYYY-NNN formatında) */
    suspend fun generateRequestNumber(): String
}
