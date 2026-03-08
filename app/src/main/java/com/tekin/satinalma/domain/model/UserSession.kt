/*
 * UserSession.kt
 * Oturum açmış kullanıcıyı tutan singleton — ekranlar arası paylaşım için kullanılır.
 * Gerçek uygulamada DataStore veya başka kalıcı mekanizma kullanılmalıdır.
 */
package com.tekin.satinalma.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Oturum yöneticisi — giriş yapan kullanıcı bilgisini tutar
 */
@Singleton
class UserSession @Inject constructor() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    /** Kullanıcıyı oturuma kaydeder */
    fun setUser(user: User) {
        _currentUser.value = user
    }

    /** Oturumu temizler */
    fun clearUser() {
        _currentUser.value = null
    }

    /** Oturum açmış kullanıcıyı döner (yoksa null) */
    fun getUser(): User? = _currentUser.value
}
