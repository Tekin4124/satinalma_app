/*
 * UpdateStatusUseCase.kt
 * Satın alma talebinin malzeme alım durumunu güncelleme iş kuralını kapsar.
 */
package com.tekin.satinalma.domain.usecase

import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.repository.PurchaseRepository
import javax.inject.Inject

/**
 * Talep durumunu güncelleyen use case
 */
class UpdateStatusUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {
    suspend operator fun invoke(requestId: String, status: MaterialStatus) {
        repository.updateStatus(requestId, status)
    }
}
