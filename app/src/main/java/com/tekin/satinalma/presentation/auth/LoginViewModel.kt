/*
 * LoginViewModel.kt
 * Giriş ekranı ViewModel'i.
 * Mock kullanıcılar arasından kullanıcı adı ve rol eşleşmesi yaparak giriş sağlar.
 */
package com.tekin.satinalma.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserRole
import com.tekin.satinalma.domain.model.UserSession
import com.tekin.satinalma.domain.model.mockUsers
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
 * Giriş ekranı ViewModel'i — mock kullanıcı doğrulaması ve oturum yönetimi
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userSession: UserSession
) : ViewModel() {

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

    /**
     * Giriş işlemini başlatır.
     * Mock kullanıcılar listesinden kullanıcı adı + rol eşleşmesi arar.
     * Eşleşme bulunamazsa geri dönüş olarak kullanıcı adından yeni kullanıcı üretir.
     */
    fun login() {
        val state = _uiState.value

        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Kullanıcı adı ve şifre boş bırakılamaz") }
            return
        }

        if (state.selectedRole == null) {
            _uiState.update { it.copy(errorMessage = "Lütfen bir rol seçin") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            delay(600)

            // Mock kullanıcılar arasında ara; bulamazsan otomatik üret
            val user = mockUsers.firstOrNull {
                it.username.equals(state.username, ignoreCase = true) && it.role == state.selectedRole
            } ?: User(
                id = "user-${state.username.lowercase()}",
                username = state.username,
                fullName = state.username.replaceFirstChar { c -> c.uppercaseChar() },
                role = state.selectedRole
            )

            userSession.setUser(user)
            _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
