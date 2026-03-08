/*
 * User.kt
 * Sisteme giriş yapan kullanıcıyı temsil eden domain model sınıfı.
 * Kullanıcı adı, tam adı ve rolü içerir.
 */
package com.tekin.satinalma.domain.model

/**
 * Kullanıcı domain modeli
 */
data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val licensePlate: String? = null  // Sadece şoförler için
)
