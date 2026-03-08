/*
 * DriverViewModel.kt
 * Şoför ekranı ViewModel'i.
 * Talep üstlenme, durum güncelleme, itiraz gönderme ve yeni talep işaretleme.
 */
package com.tekin.satinalma.presentation.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.MaterialStatus
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserSession
import com.tekin.satinalma.domain.repository.PurchaseRepository
import com.tekin.satinalma.domain.usecase.GetPurchaseRequestsUseCase
import com.tekin.satinalma.domain.usecase.UpdateStatusUseCase
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
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState: StateFlow<DriverUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(DriverFormState())
    val formState: StateFlow<DriverFormState> = _formState.asStateFlow()

    private val _notifications = MutableStateFlow(emptyList<AppNotification>())

    /** Oturum açmış şoförün ID'si */
    private val currentDriverId: String get() = userSession.getUser()?.id ?: ""

    init {
        loadRequests()
        loadNotifications()
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

    private fun loadNotifications() {
        val userId = userSession.getUser()?.id ?: return
        repository.getNotifications(userId)
            .onEach { notifs -> _notifications.value = notifs }
            .launchIn(viewModelScope)
    }

    /** Oturum açmış kullanıcıyı döner */
    fun getCurrentUser(): User? = userSession.getUser()

    /** Bildirim listesini döner */
    fun getNotifications(): List<AppNotification> = _notifications.value

    /** Bildirimi okundu işaretler */
    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch { repository.markNotificationRead(notificationId) }
    }

    /** Belirtilen talebi form için yükler ve okundu işaretler */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            _formState.update { _ ->
                DriverFormState(
                    request = request,
                    selectedStatus = request?.materialStatus ?: MaterialStatus.PENDING
                )
            }
            // Talebi okundu (görüldü) olarak işaretle
            if (currentDriverId.isNotBlank()) {
                repository.markAsSeen(requestId, currentDriverId)
            }
        }
    }

    fun onStatusSelected(status: MaterialStatus) {
        _formState.update { it.copy(selectedStatus = status) }
    }

    /** Talebi şoföre atar (üstüne al) */
    fun takeOverRequest() {
        val requestId = _formState.value.request?.id ?: return
        val driver = userSession.getUser() ?: return

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.assignDriver(requestId, driver.id, driver.licensePlate ?: "")
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

    /** Malzeme alım durumunu günceller — sadece atanmış şoför yapabilir */
    fun updateStatus() {
        val form = _formState.value
        val requestId = form.request?.id ?: return
        val newStatus = form.selectedStatus

        // Sadece atanan şoför güncelleme yapabilir
        if (form.request.assignedDriverId != currentDriverId) {
            _formState.update { it.copy(errorMessage = "Sadece atandığınız taleplerin durumunu güncelleyebilirsiniz") }
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

    /** Şoför itirazı gönderir */
    fun submitObjection(reason: String) {
        val requestId = _formState.value.request?.id ?: return

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
