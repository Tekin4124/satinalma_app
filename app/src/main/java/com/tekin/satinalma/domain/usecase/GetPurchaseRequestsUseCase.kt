/*
 * GetPurchaseRequestsUseCase.kt
 * Tüm satın alma taleplerini listeleme iş kuralını kapsar.
 * Repository'den Flow olarak veri akışı sağlar.
 */
package com.tekin.satinalma.domain.usecase

import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Tüm satın alma taleplerini dönen use case
 */
class GetPurchaseRequestsUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    operator fun invoke(): Flow<List<PurchaseRequest>> {
        return repository.getAllRequests()
    }
}
