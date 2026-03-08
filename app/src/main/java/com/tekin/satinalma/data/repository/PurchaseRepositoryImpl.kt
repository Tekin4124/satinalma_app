/*
 * PurchaseRepositoryImpl.kt
 * PurchaseRepository arayüzünün gerçekleştirimi.
 * Şimdilik bellek içi (in-memory) mock veri kullanır.
 * Gerçek uygulamada Remote/Local data source'larla değiştirilebilir.
 */
package com.tekin.satinalma.data.repository

import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.mockUsers
import com.tekin.satinalma.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bellek içi mock veri deposu kullanan repository implementasyonu
 */
@Singleton
class PurchaseRepositoryImpl @Inject constructor() : PurchaseRepository {

    private val _requests = MutableStateFlow(generateMockRequests())
    private val _notifications = MutableStateFlow(emptyList<AppNotification>())

    override fun getAllRequests(): Flow<List<PurchaseRequest>> = _requests.asStateFlow()

    override suspend fun getRequestById(id: String): PurchaseRequest? {
        return _requests.value.find { it.id == id }
    }

    override suspend fun createRequest(request: PurchaseRequest) {
        _requests.update { current -> current + request }
        // Yeni talep bildirimi oluştur
        val drivers = mockUsers.filter { it.licensePlate != null }
        val officeUsers = mockUsers.filter { it.role.name == "LOGISTICS_OFFICE" }
        val targets = (drivers + officeUsers).map { it.id }
        val notifs = targets.map { userId ->
            AppNotification(
                id = UUID.randomUUID().toString(),
                message = "Yeni satın alma talebi oluşturuldu: ${request.requestNumber}",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetUserId = userId
            )
        }
        _notifications.update { current -> current + notifs }
    }

    override suspend fun updateRequest(request: PurchaseRequest) {
        _requests.update { current ->
            current.map { if (it.id == request.id) request.copy(updatedAt = System.currentTimeMillis()) else it }
        }
    }

    override suspend fun updateStatus(requestId: String, status: MaterialStatus) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(materialStatus = status, updatedAt = System.currentTimeMillis())
                } else {
                    request
                }
            }
        }
        // Durum değişikliği bildirimi
        val req = _requests.value.find { it.id == requestId } ?: return
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            message = "Talep durumu güncellendi: ${req.requestNumber} → ${status.displayName}",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            targetUserId = req.assignedPurchasingStaffId
        )
        _notifications.update { current -> current + notif }
    }

    override suspend fun assignDriver(requestId: String, driverId: String, licensePlate: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(
                        assignedDriverId = driverId,
                        licensePlate = licensePlate,
                        materialStatus = MaterialStatus.IN_PROGRESS,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    request
                }
            }
        }
        // Şoföre bildirim gönder
        val req = _requests.value.find { it.id == requestId } ?: return
        val notif = AppNotification(
            id = UUID.randomUUID().toString(),
            message = "Size yeni bir iş atandı: ${req.requestNumber}",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            targetUserId = driverId
        )
        _notifications.update { current -> current + notif }
    }

    override suspend fun cancelRequest(requestId: String, reason: String?, cancelledBy: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(
                        materialStatus = MaterialStatus.CANCELLED,
                        cancellationReason = reason,
                        cancelledBy = cancelledBy,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    request
                }
            }
        }
    }

    override suspend fun submitObjection(requestId: String, driverId: String, reason: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(
                        hasObjection = true,
                        objectionReason = reason,
                        objectionBy = driverId,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    request
                }
            }
        }
        // Sevkiyat ofisine bildirim gönder
        val req = _requests.value.find { it.id == requestId } ?: return
        val officeUser = mockUsers.firstOrNull { it.role.name == "LOGISTICS_OFFICE" }
        officeUser?.let { user ->
            val notif = AppNotification(
                id = UUID.randomUUID().toString(),
                message = "Şoför itiraz etti: ${req.requestNumber} — $reason",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetUserId = user.id
            )
            _notifications.update { current -> current + notif }
        }
    }

    override suspend fun redirectRequest(requestId: String, targetRequestId: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(
                        redirectedToRequestId = targetRequestId,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    request
                }
            }
        }
    }

    override suspend fun markAsSeen(requestId: String, userId: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId && userId !in request.seenByUserIds) {
                    request.copy(seenByUserIds = request.seenByUserIds + userId)
                } else {
                    request
                }
            }
        }
    }

    override fun getDrivers(): Flow<List<User>> =
        MutableStateFlow(mockUsers.filter { it.licensePlate != null }).asStateFlow()

    override fun getNotifications(userId: String): Flow<List<AppNotification>> =
        _notifications.map { list -> list.filter { it.targetUserId == userId } }

    override suspend fun markNotificationRead(notificationId: String) {
        _notifications.update { current ->
            current.map { if (it.id == notificationId) it.copy(isRead = true) else it }
        }
    }

    override suspend fun generateRequestNumber(): String {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val existing = _requests.value
            .mapNotNull { req ->
                val prefix = "TLP-$year-"
                if (req.requestNumber.startsWith(prefix)) {
                    req.requestNumber.removePrefix(prefix).toIntOrNull()
                } else null
            }
        val nextSeq = (existing.maxOrNull() ?: 0) + 1
        return "TLP-$year-%03d".format(nextSeq)
    }

    /** Test ve demo amaçlı başlangıç verileri — gerçek şirket verisi kullanılmamıştır */
    private fun generateMockRequests(): List<PurchaseRequest> {
        val baseTime = System.currentTimeMillis()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return listOf(
            PurchaseRequest(
                id = "mock-001",
                requestNumber = "TLP-$year-001",
                itemToPurchase = "Çelik Profil 100x50mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                contactNumber = "0312 000 00 01",
                productDimensions = "100x50x2000mm",
                productWeight = "25 kg",
                urgencyLevel = UrgencyLevel.HIGH,
                purchaseDate = "2024-12-15",
                notes = "Acil sipariş, stok kritik seviyede",
                licensePlate = "34 ABC 123",
                materialStatus = MaterialStatus.IN_PROGRESS,
                assignedDriverId = "3",
                assignedPurchasingStaffId = "1",
                createdAt = baseTime - 86400000L,
                updatedAt = baseTime - 3600000L,
                isNew = false,
                seenByUserIds = setOf("3")
            ),
            PurchaseRequest(
                id = "mock-002",
                requestNumber = "TLP-$year-002",
                itemToPurchase = "Boru 2 inç",
                companyName = "Örnek Malzeme Ltd.",
                companyAddress = "Atatürk Cad. No:45, İstanbul",
                contactNumber = "0216 000 00 02",
                productDimensions = "2 inç x 6 metre",
                productWeight = "12 kg/adet",
                urgencyLevel = UrgencyLevel.NORMAL,
                purchaseDate = "2024-12-20",
                notes = "10 adet gerekli",
                licensePlate = "",
                materialStatus = MaterialStatus.PENDING,
                assignedDriverId = null,
                assignedPurchasingStaffId = "1",
                createdAt = baseTime - 43200000L,
                updatedAt = baseTime - 43200000L,
                isNew = true
            ),
            PurchaseRequest(
                id = "mock-003",
                requestNumber = "TLP-$year-003",
                itemToPurchase = "Vida M8x40",
                companyName = "Demo Tedarik San. Tic.",
                companyAddress = "Sanayi Mah. Fabrika Sok. No:8, İzmir",
                contactNumber = "0232 000 00 03",
                productDimensions = "M8 x 40mm",
                productWeight = "0.5 kg/100 adet",
                urgencyLevel = UrgencyLevel.LOW,
                purchaseDate = "2024-12-25",
                notes = "500 adet sipariş verilecek",
                licensePlate = "06 XYZ 789",
                materialStatus = MaterialStatus.PURCHASED,
                assignedDriverId = "4",
                assignedPurchasingStaffId = "1",
                createdAt = baseTime - 172800000L,
                updatedAt = baseTime - 7200000L,
                isNew = false,
                seenByUserIds = setOf("4")
            ),
            PurchaseRequest(
                id = "mock-004",
                requestNumber = "TLP-$year-004",
                itemToPurchase = "Alüminyum Levha 3mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                contactNumber = "0312 000 00 01",
                productDimensions = "1000x2000x3mm",
                productWeight = "16 kg/adet",
                urgencyLevel = UrgencyLevel.URGENT,
                purchaseDate = "2024-12-12",
                notes = "Proje için acil gerekli",
                licensePlate = "35 DEF 456",
                materialStatus = MaterialStatus.DELIVERED,
                assignedDriverId = "3",
                assignedPurchasingStaffId = "1",
                createdAt = baseTime - 259200000L,
                updatedAt = baseTime - 1800000L,
                isNew = false,
                seenByUserIds = setOf("3")
            ),
            PurchaseRequest(
                id = "mock-005",
                requestNumber = "TLP-$year-005",
                itemToPurchase = "Somun M10",
                companyName = "Örnek Malzeme Ltd.",
                companyAddress = "Atatürk Cad. No:45, İstanbul",
                contactNumber = "0216 000 00 02",
                productDimensions = "M10",
                productWeight = "1 kg/100 adet",
                urgencyLevel = UrgencyLevel.NORMAL,
                purchaseDate = "2025-01-05",
                notes = "",
                licensePlate = "",
                materialStatus = MaterialStatus.PENDING,
                assignedDriverId = null,
                assignedPurchasingStaffId = "1",
                createdAt = baseTime - 7200000L,
                updatedAt = baseTime - 7200000L,
                isNew = true
            )
        )
    }
}


