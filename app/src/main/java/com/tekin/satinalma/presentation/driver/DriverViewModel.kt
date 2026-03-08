/*
 * DriverViewModel.kt
 * Şoför ekranı ViewModel'i.
 * Talep üstlenme, durum güncelleme ve itiraz gönderme işlemlerini yönetir.
 * Şoförün kendi plakası hesabından otomatik alınır.
 */
package com.tekin.satinalma.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.repository.PurchaseRepository
import com.tekin.satinalma.domain.usecase.GetPurchaseRequestsUseCase
import com.tekin.satinalma.domain.usecase.UpdateStatusUseCase
import com.tekin.satinalma.util.Constants
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

/** Şoför ekranı UI durumu */
data class DriverUiState(
    val allRequests: List<PurchaseRequest> = emptyList(),
    val myRequests: List<PurchaseRequest> = emptyList(),
    val unassignedRequests: List<PurchaseRequest> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/** Şoför talep detay formu durumu */
data class DriverFormState(
    val request: PurchaseRequest? = null,
    val selectedStatus: MaterialStatus = MaterialStatus.PENDING,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Şoför ekranı ViewModel'i
 */
@HiltViewModel
class DriverViewModel @Inject constructor(
    private val getPurchaseRequestsUseCase: GetPurchaseRequestsUseCase,
    private val updateStatusUseCase: UpdateStatusUseCase,
    private val repository: PurchaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(DriverFormState())
    val formState: StateFlow<DriverFormState> = _formState.asStateFlow()

    /** Giriş yapmış şoförün ID'si — public for screen access */
    val currentDriverId: String
        get() = sessionManager.currentUser?.id ?: Constants.MockUsers.DRIVER_ID

    /** Giriş yapmış şoförün plakası */
    val currentDriverLicensePlate: String
        get() = sessionManager.currentUser?.licensePlate ?: ""

    init {
        loadRequests()
    }

    private fun loadRequests() {
        getPurchaseRequestsUseCase()
            .onEach { requests ->
                val myRequests = requests.filter { it.assignedDriverId == currentDriverId }
                val unassigned = requests.filter { it.assignedDriverId == null }
                _uiState.update {
                    it.copy(
                        allRequests = requests,
                        myRequests = myRequests,
                        unassignedRequests = unassigned,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /** Belirtilen talebi form için yükler */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            _formState.update { _ ->
                DriverFormState(
                    request = request,
                    selectedStatus = request?.materialStatus ?: MaterialStatus.PENDING
                )
            }
        }
    }

    fun onStatusSelected(status: MaterialStatus) {
        _formState.update { it.copy(selectedStatus = status) }
    }

    /** Talebi şoföre atar — şoförün kendi plakasını otomatik kullanır */
    fun takeOverRequest() {
        val requestId = _formState.value.request?.id ?: return
        val licensePlate = currentDriverLicensePlate

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.assignDriver(requestId, currentDriverId, licensePlate)
            val updatedRequest = repository.getRequestById(requestId)
            _formState.update { state ->
                state.copy(
                    isSaving = false,
                    request = updatedRequest,
                    successMessage = "Talep başarıyla üstlenildi"
                )
            }
        }
    }

    /** Malzeme alım durumunu günceller — sadece şoföre atanmış talepler için */
    fun updateStatus() {
        val form = _formState.value
        val requestId = form.request?.id ?: return
        val newStatus = form.selectedStatus

        if (form.request?.assignedDriverId != currentDriverId) {
            _formState.update { it.copy(errorMessage = "Sadece üstünüzdeki taleplerin durumunu güncelleyebilirsiniz") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            updateStatusUseCase(requestId, newStatus)
            val updatedRequest = repository.getRequestById(requestId)
            _formState.update { state ->
                state.copy(
                    isSaving = false,
                    request = updatedRequest,
                    successMessage = "Durum güncellendi: ${newStatus.displayName}"
                )
            }
        }
    }

    /** Şoförden itiraz gönderir */
    fun submitObjection(reason: String) {
        val requestId = _formState.value.request?.id ?: return

        if (reason.isBlank()) {
            _formState.update { it.copy(errorMessage = "İtiraz nedeni boş bırakılamaz") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.submitObjection(requestId, currentDriverId, reason)
            val updatedRequest = repository.getRequestById(requestId)
            _formState.update { state ->
                state.copy(
                    isSaving = false,
                    request = updatedRequest,
                    successMessage = "İtirazınız iletildi"
                )
            }
        }
    }

    fun clearFormMessages() {
        _formState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
