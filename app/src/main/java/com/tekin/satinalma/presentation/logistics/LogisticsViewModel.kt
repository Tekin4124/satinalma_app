/*
 * LogisticsViewModel.kt
 * Sevkiyat ofis ekranı ViewModel'i.
 * Talep listesi, plaka atama ve talep yönlendirme işlemlerini yönetir.
 */
package com.tekin.satinalma.presentation.logistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.repository.PurchaseRepository
import com.tekin.satinalma.domain.usecase.GetPurchaseRequestsUseCase
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
    val editableLicensePlate: String = "",
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
    private val repository: PurchaseRepository
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

    /** Belirtilen talebi form için yükler */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            _formState.update { _ ->
                LogisticsFormState(
                    request = request,
                    editableLicensePlate = request?.licensePlate ?: ""
                )
            }
        }
    }

    fun onLicensePlateChange(value: String) {
        _formState.update { it.copy(editableLicensePlate = value) }
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
