/*
 * CreatePurchaseUseCase.kt
 * Yeni satın alma talebi oluşturma iş kuralını kapsar.
 * Talep numarası repository üzerinden otomatik olarak üretilir (TLP-YYYY-NNN formatı).
 */
package com.tekin.satinalma.domain.usecase

import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.repository.PurchaseRepository
import javax.inject.Inject

/**
 * Yeni satın alma talebi oluşturan use case
 */
class CreatePurchaseUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(request: PurchaseRequest) {
        val requestWithNumber = if (request.requestNumber.isBlank()) {
            request.copy(requestNumber = repository.generateRequestNumber())
        } else {
            request
        }
        repository.createRequest(requestWithNumber)
    }
}
