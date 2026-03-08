/*
 * LogisticsViewModel.kt
 * Sevkiyat ofis ekranı ViewModel'i.
 * Talep listesi, şoför atama ve talep iptal işlemlerini yönetir.
 */
package com.tekin.satinalma.presentation.logistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.repository.PurchaseRepository
import com.tekin.satinalma.domain.usecase.GetPurchaseRequestsUseCase
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

/** Sevkiyat ofis ekranı UI durumu */
data class LogisticsUiState(
    val requests: List<PurchaseRequest> = emptyList(),
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/** Sevkiyat ofis talep detay formu durumu */
data class LogisticsFormState(
    val request: PurchaseRequest? = null,
    val drivers: List<User> = emptyList(),
    val selectedDriver: User? = null,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Sevkiyat ofis ekranı ViewModel'i
 */
@HiltViewModel
class LogisticsViewModel @Inject constructor(
    private val getPurchaseRequestsUseCase: GetPurchaseRequestsUseCase,
    private val repository: PurchaseRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LogisticsFormState())
    val formState: StateFlow<LogisticsFormState> = _formState.asStateFlow()

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

    /** Belirtilen talebi form için yükler, şoför listesi de yüklenir */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            val drivers = repository.getDrivers()
            val assignedDriver = drivers.find { it.id == request?.assignedDriverId }
            _formState.update { _ ->
                LogisticsFormState(
                    request = request,
                    drivers = drivers,
                    selectedDriver = assignedDriver
                )
            }
        }
    }

    fun onDriverSelected(driver: User) {
        _formState.update { it.copy(selectedDriver = driver) }
    }

    /** Seçili şoförü talebe atar */
    fun assignDriver() {
        val form = _formState.value
        val requestId = form.request?.id ?: return
        val driver = form.selectedDriver ?: run {
            _formState.update { it.copy(errorMessage = "Lütfen bir şoför seçin") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.assignDriver(requestId, driver.id, driver.licensePlate ?: "")
            val updatedRequest = repository.getRequestById(requestId)
            _formState.update { state ->
                state.copy(
                    isSaving = false,
                    request = updatedRequest,
                    successMessage = "${driver.fullName} şoföre atandı"
                )
            }
        }
    }

    /** Talebi iptal eder */
    fun cancelRequest(reason: String) {
        val requestId = _formState.value.request?.id ?: return
        val cancelledBy = sessionManager.currentUser?.fullName ?: "Sevkiyat Ofis"

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.cancelRequest(requestId, reason.ifBlank { null }, cancelledBy)
            val updatedRequest = repository.getRequestById(requestId)
            _formState.update { state ->
                state.copy(
                    isSaving = false,
                    request = updatedRequest,
                    successMessage = "İş iptal edildi"
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
