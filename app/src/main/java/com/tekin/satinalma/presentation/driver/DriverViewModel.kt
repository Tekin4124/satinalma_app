/*
 * DriverViewModel.kt
 * Şoför ekranı ViewModel'i.
 * Talep üstlenme, durum güncelleme ve plaka güncelleme işlemlerini yönetir.
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
    val editableLicensePlate: String = "",
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
    private val repository: PurchaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(DriverFormState())
    val formState: StateFlow<DriverFormState> = _formState.asStateFlow()

    /** Şu anki şoförün ID'si — gerçek uygulamada oturum yönetiminden alınır */
    private val currentDriverId = Constants.MockUsers.DRIVER_ID

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
                    editableLicensePlate = request?.licensePlate ?: "",
                    selectedStatus = request?.materialStatus ?: MaterialStatus.PENDING
                )
            }
        }
    }

    fun onLicensePlateChange(value: String) {
        _formState.update { it.copy(editableLicensePlate = value) }
    }

    fun onStatusSelected(status: MaterialStatus) {
        _formState.update { it.copy(selectedStatus = status) }
    }

    /** Talebi şoföre atar (üstüne al) */
    fun takeOverRequest() {
        val requestId = _formState.value.request?.id ?: return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.assignDriver(requestId, currentDriverId)
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

    /** Malzeme alım durumunu günceller */
    fun updateStatus() {
        val requestId = _formState.value.request?.id ?: return
        val newStatus = _formState.value.selectedStatus

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

    /** Plaka bilgisini günceller */
    fun saveLicensePlate() {
        val form = _formState.value
        val requestId = form.request?.id ?: return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.updateLicensePlate(requestId, form.editableLicensePlate)
            _formState.update { it.copy(isSaving = false, successMessage = "Plaka güncellendi") }
        }
    }

    fun clearFormMessages() {
        _formState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
