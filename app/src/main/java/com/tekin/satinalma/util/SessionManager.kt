/*
 * SessionManager.kt
 * Oturum yönetimi — giriş yapan kullanıcı bilgisini tüm uygulama genelinde saklar.
 */
package com.tekin.satinalma.util

import com.tekin.satinalma.domain.model.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton oturum yöneticisi — mevcut kullanıcıyı saklar
 */
@Singleton
class SessionManager @Inject constructor() {

    /** Giriş yapmış kullanıcı */
    var currentUser: User? = null
        private set

    /** Kullanıcıyı oturuma ayarlar */
    fun setUser(user: User) {
        currentUser = user
    }

    /** Oturumu kapatır */
    fun clearUser() {
        currentUser = null
    }
}
