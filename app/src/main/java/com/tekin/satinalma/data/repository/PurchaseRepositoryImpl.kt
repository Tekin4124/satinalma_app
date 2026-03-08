/*
 * PurchaseRepositoryImpl.kt
 * PurchaseRepository arayüzünün gerçekleştirimi.
 * Şimdilik bellek içi (in-memory) mock veri kullanır.
 * Gerçek uygulamada Remote/Local data source'larla değiştirilebilir.
 */
package com.tekin.satinalma.data.repository

import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bellek içi mock veri deposu kullanan repository implementasyonu
 */
@Singleton
class PurchaseRepositoryImpl @Inject constructor() : PurchaseRepository {

    private val _requests = MutableStateFlow(generateMockRequests())

    override fun getAllRequests(): Flow<List<PurchaseRequest>> = _requests.asStateFlow()

    override suspend fun getRequestById(id: String): PurchaseRequest? {
        return _requests.value.find { it.id == id }
    }

    override suspend fun createRequest(request: PurchaseRequest) {
        _requests.update { current -> current + request }
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
    }

    override suspend fun assignDriver(requestId: String, driverId: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(
                        assignedDriverId = driverId,
                        materialStatus = MaterialStatus.IN_PROGRESS,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    request
                }
            }
        }
    }

    override suspend fun updateLicensePlate(requestId: String, plate: String) {
        _requests.update { current ->
            current.map { request ->
                if (request.id == requestId) {
                    request.copy(licensePlate = plate, updatedAt = System.currentTimeMillis())
                } else {
                    request
                }
            }
        }
    }

    /** Test ve demo amaçlı başlangıç verileri — gerçek şirket verisi kullanılmamıştır */
    private fun generateMockRequests(): List<PurchaseRequest> {
        val baseTime = System.currentTimeMillis()
        return listOf(
            PurchaseRequest(
                id = "mock-001",
                requestNumber = "TLB-10001",
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
                assignedDriverId = "driver-001",
                assignedPurchasingStaffId = "staff-001",
                createdAt = baseTime - 86400000L,
                updatedAt = baseTime - 3600000L
            ),
            PurchaseRequest(
                id = "mock-002",
                requestNumber = "TLB-10002",
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
                assignedPurchasingStaffId = "staff-001",
                createdAt = baseTime - 43200000L,
                updatedAt = baseTime - 43200000L
            ),
            PurchaseRequest(
                id = "mock-003",
                requestNumber = "TLB-10003",
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
                assignedDriverId = "driver-002",
                assignedPurchasingStaffId = "staff-002",
                createdAt = baseTime - 172800000L,
                updatedAt = baseTime - 7200000L
            ),
            PurchaseRequest(
                id = "mock-004",
                requestNumber = "TLB-10004",
                itemToPurchase = "Alüminyum Levha 3mm",
                companyName = "Test Firma A.Ş.",
                companyAddress = "Organize Sanayi Bölgesi, Ankara",
                contactNumber = "0312 000 00 01",
                productDimensions = "1000x2000x3mm",
                productWeight = "16 kg/adet",
                urgencyLevel = UrgencyLevel.URGENT,
                purchaseDate = "2024-12-12",
                notes = "Proje için acil gerekli",
                licensePlate = "35 TEST 456",
                materialStatus = MaterialStatus.DELIVERED,
                assignedDriverId = "driver-001",
                assignedPurchasingStaffId = "staff-002",
                createdAt = baseTime - 259200000L,
                updatedAt = baseTime - 1800000L
            ),
            PurchaseRequest(
                id = "mock-005",
                requestNumber = "TLB-10005",
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
                assignedPurchasingStaffId = "staff-001",
                createdAt = baseTime - 7200000L,
                updatedAt = baseTime - 7200000L
            )
        )
    }
}
