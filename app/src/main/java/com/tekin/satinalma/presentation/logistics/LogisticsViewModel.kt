/*
 * LogisticsViewModel.kt
 * Sevkiyat ofis ekranı ViewModel'i.
 * Talep listesi, şoför seçimi/atama, iptal ve yönlendirme işlemlerini yönetir.
 */
package com.tekin.satinalma.presentation.logistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.AppNotification
import com.tekin.satinalma.domain.model.PurchaseRequest
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserSession
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
    val selectedDriver: User? = null,
    val redirectTargetId: String? = null,
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
    private val userSession: UserSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogisticsUiState())
    val uiState: StateFlow<LogisticsUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(LogisticsFormState())
    val formState: StateFlow<LogisticsFormState> = _formState.asStateFlow()

    private val _drivers = MutableStateFlow(emptyList<User>())
    val drivers: StateFlow<List<User>> = _drivers.asStateFlow()

    private val _notifications = MutableStateFlow(emptyList<AppNotification>())

    init {
        loadRequests()
        loadDrivers()
        loadNotifications()
    }

    private fun loadRequests() {
        getPurchaseRequestsUseCase()
            .onEach { requests ->
                _uiState.update { it.copy(requests = requests, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadDrivers() {
        repository.getDrivers()
            .onEach { drivers -> _drivers.value = drivers }
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

    /** Belirtilen talebi form için yükler */
    fun loadRequest(requestId: String) {
        viewModelScope.launch {
            val request = repository.getRequestById(requestId)
            _formState.update { _ ->
                LogisticsFormState(request = request)
            }
        }
    }

    fun onDriverSelected(driver: User) {
        _formState.update { it.copy(selectedDriver = driver) }
    }

    fun onRedirectTargetSelected(requestId: String?) {
        _formState.update { it.copy(redirectTargetId = requestId) }
    }

    /** Seçilen şoförü talebe atar */
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
            val updated = repository.getRequestById(requestId)
            _formState.update { it.copy(isSaving = false, request = updated, successMessage = "Şoför atandı") }
        }
    }

    /** Talebi iptal eder */
    fun cancelRequest(requestId: String, reason: String?) {
        val currentUserId = userSession.getUser()?.id ?: "2"
        viewModelScope.launch {
            repository.cancelRequest(requestId, reason, currentUserId)
            val updated = repository.getRequestById(requestId)
            _formState.update { it.copy(request = updated, successMessage = "Talep iptal edildi") }
            _uiState.update { it.copy(successMessage = "Talep iptal edildi") }
        }
    }

    /** Talebi yönlendirir */
    fun redirectRequest() {
        val form = _formState.value
        val requestId = form.request?.id ?: return
        val targetId = form.redirectTargetId ?: run {
            _formState.update { it.copy(errorMessage = "Lütfen hedef talebi seçin") }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            repository.redirectRequest(requestId, targetId)
            val updated = repository.getRequestById(requestId)
            _formState.update { it.copy(isSaving = false, request = updated, successMessage = "Talep yönlendirildi") }
        }
    }

    fun clearFormMessages() {
        _formState.update { it.copy(successMessage = null, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
