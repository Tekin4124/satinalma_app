/*
 * User.kt
 * Sisteme giriş yapan kullanıcıyı temsil eden domain model sınıfı.
 * Kullanıcı adı, tam adı, rolü ve (şoför ise) plaka bilgisini içerir.
 */
package com.tekin.satinalma.domain.model

/**
 * Kullanıcı domain modeli
 * licensePlate: Sadece DRIVER rolündeki kullanıcılar için araç plakası
 */
data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val role: UserRole,
    val licensePlate: String? = null
) {
    /** Kullanıcı adının baş harflerini döndürür (avatar için) */
    fun initials(): String {
        val parts = fullName.trim().split(" ")
        return when {
            parts.size >= 2 -> "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
            parts.size == 1 && parts[0].isNotEmpty() -> parts[0].first().uppercaseChar().toString()
            else -> "?"
        }
    }
}

/** Test / demo amaçlı mock kullanıcı listesi — gerçek kullanıcı verisi değildir */
val mockUsers = listOf(
    User("1", "mehmet", "Mehmet Demir", UserRole.PURCHASING),
    User("2", "ayse", "Ayşe Kaya", UserRole.LOGISTICS_OFFICE),
    User("3", "ahmet", "Ahmet Yılmaz", UserRole.DRIVER, "34 ABC 123"),
    User("4", "ali", "Ali Öztürk", UserRole.DRIVER, "06 XYZ 789"),
    User("5", "veli", "Veli Şahin", UserRole.DRIVER, "35 DEF 456")
)
