/*
 * PurchasingViewModel.kt
 * Satınalma ekranları ViewModel'i.
 * Talep listesi, yeni talep oluşturma, güncelleme ve iptal işlemlerini yönetir.
 */
package com.tekin.satinalma.presentation.purchasing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.UrgencyLevel
import com.tekin.satinalma.domain.usecase.CreatePurchaseUseCase
import com.tekin.satinalma.domain.usecase.GetPurchaseRequestsUseCase
import com.tekin.satinalma.domain.repository.PurchaseRepository
import com.tekin.satinalma.util.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Satınalma ekranı UI durumu */
data class PurchasingUiState(
    val requests: List<PurchaseRequest> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/** Satınalma detay/form ekranı UI durumu */
data class PurchasingFormState(
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
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val saveSuccess: Boolean = false,
    val cancelSuccess: Boolean = false
)

/**
 * Satınalma ekranları ViewModel'i
 */
@HiltViewModel
class PurchasingViewModel @Inject constructor(
    private val getPurchaseRequestsUseCase: GetPurchaseRequestsUseCase,
    private val createPurchaseUseCase: CreatePurchaseUseCase,
    private val repository: PurchaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchasingUiState())
    val uiState: StateFlow<PurchasingUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(PurchasingFormState())
    val formState: StateFlow<PurchasingFormState> = _formState.asStateFlow()

    init {
        loadRequests()
    }

    private fun loadRequests() {
        getPurchaseRequestsUseCase()
            .onEach { requests ->
                _uiState.update { it.copy(requests = requests, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    /** Düzenleme formu için mevcut talebi yükler */
    fun loadRequestForEdit(requestId: String) {
        if (requestId == "new") {
            _formState.value = PurchasingFormState()
            return
        }
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            request?.let {
                _formState.update { _ ->
                    PurchasingFormState(
                        requestNumber = it.requestNumber,
                        itemToPurchase = it.itemToPurchase,
                        companyName = it.companyName,
                        companyAddress = it.companyAddress,
                        contactNumber = it.contactNumber,
                        productDimensions = it.productDimensions,
                        productWeight = it.productWeight,
                        urgencyLevel = it.urgencyLevel,
                        purchaseDate = it.purchaseDate,
                        notes = it.notes
                    )
                }
            }
        }
    }

    // Form değeri güncelleyicileri
    fun onItemToPurchaseChange(value: String) = _formState.update { it.copy(itemToPurchase = value) }
    fun onCompanyNameChange(value: String) = _formState.update { it.copy(companyName = value) }
    fun onCompanyAddressChange(value: String) = _formState.update { it.copy(companyAddress = value) }
    fun onContactNumberChange(value: String) = _formState.update { it.copy(contactNumber = value) }
    fun onProductDimensionsChange(value: String) = _formState.update { it.copy(productDimensions = value) }
    fun onProductWeightChange(value: String) = _formState.update { it.copy(productWeight = value) }
    fun onUrgencyLevelChange(value: UrgencyLevel) = _formState.update { it.copy(urgencyLevel = value) }
    fun onPurchaseDateChange(value: String) = _formState.update { it.copy(purchaseDate = value) }
    fun onNotesChange(value: String) = _formState.update { it.copy(notes = value) }

    /** Yeni talep kaydeder veya mevcut talebi günceller */
    fun saveRequest(existingRequestId: String? = null) {
        val form = _formState.value

        if (form.itemToPurchase.isBlank()) {
            _formState.update { it.copy(errorMessage = "Alım yapılacak ürün alanı zorunludur") }
            return
        }
        if (form.companyName.isBlank()) {
            _formState.update { it.copy(errorMessage = "Firma adı zorunludur") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true, errorMessage = null) }

            val request = PurchaseRequest(
                id = existingRequestId ?: java.util.UUID.randomUUID().toString(),
                requestNumber = form.requestNumber,
                itemToPurchase = form.itemToPurchase,
                companyName = form.companyName,
                companyAddress = form.companyAddress,
                contactNumber = form.contactNumber,
                productDimensions = form.productDimensions,
                productWeight = form.productWeight,
                urgencyLevel = form.urgencyLevel,
                purchaseDate = form.purchaseDate,
                notes = form.notes,
                assignedPurchasingStaffId = "staff-001"
            )

            if (existingRequestId == null || existingRequestId == "new") {
                createPurchaseUseCase(request)
            } else {
                repository.updateRequest(request)
            }

            _formState.update { it.copy(isSaving = false, saveSuccess = true) }
        }
    }

    /** Talebi iptal eder */
    fun cancelRequest(requestId: String, reason: String) {
        val cancelledBy = sessionManager.currentUser?.fullName ?: "Satınalma Personeli"

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.cancelRequest(requestId, reason.ifBlank { null }, cancelledBy)
            _formState.update { it.copy(isSaving = false, cancelSuccess = true) }
        }
    }

    fun clearFormMessages() {
        _formState.update { it.copy(errorMessage = null, saveSuccess = false, cancelSuccess = false) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
