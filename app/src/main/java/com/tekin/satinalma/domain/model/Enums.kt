/*
 * Enums.kt
 * Uygulamada kullanılan tüm enum sınıflarını içerir:
 * UrgencyLevel (aciliyet seviyesi), MaterialStatus (malzeme alım durumu), UserRole (kullanıcı rolü)
 */
package com.tekin.satinalma.domain.model

/**
 * Satın alma talebinin aciliyet seviyesi
 */
enum class UrgencyLevel(val displayName: String) {
    LOW("Düşük"),
    NORMAL("Normal"),
    HIGH("Yüksek"),
    URGENT("Acil")
}

/**
 * Malzeme alım durumu — talep akışının hangi aşamasında olduğunu gösterir
 */
enum class MaterialStatus(val displayName: String) {
    PENDING("Beklemede"),
    IN_PROGRESS("Devam Ediyor"),
    PURCHASED("Satın Alındı"),
    DELIVERED("Teslim Edildi"),
    CANCELLED("İptal Edildi")
}

/**
 * Sisteme erişen kullanıcının rolü — hangi ekranın gösterileceğini belirler
 */
enum class UserRole(val displayName: String) {
    PURCHASING("Satınalma Personeli"),
    LOGISTICS_OFFICE("Sevkiyat Ofis"),
    DRIVER("Şoför")
}
