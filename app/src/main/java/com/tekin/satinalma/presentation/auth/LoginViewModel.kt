/*
 * LoginViewModel.kt
 * Giriş ekranı ViewModel'i.
 * Kullanıcı adı, şifre ve rol doğrulaması yapılır; giriş başarılıysa kullanıcı objesi döndürülür.
 */
package com.tekin.satinalma.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Giriş ekranı UI durumu */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val selectedRole: UserRole? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null
)

/**
 * Giriş ekranı ViewModel'i — kullanıcı doğrulama ve yönlendirme mantığı
 */
@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onRoleSelected(role: UserRole) {
        _uiState.update { it.copy(selectedRole = role, errorMessage = null) }
    }

    /** Giriş işlemini başlatır — test modunda her kullanıcı adı/şifre kabul edilir */
    fun login() {
        val state = _uiState.value

        // Boş alan doğrulama
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kullanıcı adı ve şifre boş bırakılamaz") }
            return
        }

        // Rol seçimi doğrulama
        if (state.selectedRole == null) {
            _uiState.update { it.copy(errorMessage = "Lütfen bir rol seçin") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Test modu: simüle edilmiş ağ gecikmesi
            delay(800)

            val user = User(
                id = "user-${state.username.lowercase()}",
                username = state.username,
                fullName = state.username,
                role = state.selectedRole
            )

            _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
