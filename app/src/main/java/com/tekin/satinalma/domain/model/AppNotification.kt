/*
 * AppNotification.kt
 * Uygulama içi bildirim modelini tanımlar.
 * Kullanıcıya yönelik sistem bildirimleri bu model üzerinden yönetilir.
 */
package com.tekin.satinalma.domain.model

/**
 * Uygulama bildirimi domain modeli
 */
data class AppNotification(
    val id: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetUserId: String
)
