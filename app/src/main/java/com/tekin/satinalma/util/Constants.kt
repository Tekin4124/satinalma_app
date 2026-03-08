/*
 * Constants.kt
 * Uygulama genelinde kullanılan sabit değerleri içerir.
 * Navigation route'ları, mock kullanıcı ID'leri ve UI sabitleri tanımlanır.
 */
package com.tekin.satinalma.util

/**
 * Uygulama sabitleri
 */
object Constants {

    /** Navigasyon rotaları */
    object Routes {
        const val LOGIN = "login"
        const val PURCHASING = "purchasing"
        const val PURCHASING_DETAIL = "purchasing_detail/{requestId}"
        const val LOGISTICS = "logistics"
        const val LOGISTICS_DETAIL = "logistics_detail/{requestId}"
        const val DRIVER = "driver"
        const val DRIVER_DETAIL = "driver_detail/{requestId}"

        fun purchasingDetail(requestId: String) = "purchasing_detail/$requestId"
        fun logisticsDetail(requestId: String) = "logistics_detail/$requestId"
        fun driverDetail(requestId: String) = "driver_detail/$requestId"
    }

    /** Navigation argüman anahtarları */
    object NavArgs {
        const val REQUEST_ID = "requestId"
    }

    /** Mock kullanıcı ID'leri — test verisi */
    object MockUsers {
        const val PURCHASING_STAFF_ID = "staff-001"
        const val DRIVER_ID = "driver-001"
        const val LOGISTICS_ID = "logistics-001"
    }

    /** Mock kullanıcılar — test ve demo verisi */
    object MockUserData {
        val USERS = listOf(
            com.tekin.satinalma.domain.model.User("1", "mehmet", "Mehmet Demir", com.tekin.satinalma.domain.model.UserRole.PURCHASING, null),
            com.tekin.satinalma.domain.model.User("2", "ayse", "Ayşe Kaya", com.tekin.satinalma.domain.model.UserRole.LOGISTICS_OFFICE, null),
            com.tekin.satinalma.domain.model.User("3", "ahmet", "Ahmet Yılmaz", com.tekin.satinalma.domain.model.UserRole.DRIVER, "34 ABC 123"),
            com.tekin.satinalma.domain.model.User("4", "ali", "Ali Öztürk", com.tekin.satinalma.domain.model.UserRole.DRIVER, "06 XYZ 789"),
            com.tekin.satinalma.domain.model.User("5", "veli", "Veli Şahin", com.tekin.satinalma.domain.model.UserRole.DRIVER, "35 DEF 456")
        )

        fun findByUsername(username: String): com.tekin.satinalma.domain.model.User? =
            USERS.find { it.username.lowercase() == username.lowercase() }
    }

    /** Yeni talep oluşturulduğunda kullanılan yer tutucu ID */
    const val NEW_REQUEST_ID = "new"

    /** Talep numarası öneki */
    const val REQUEST_NUMBER_PREFIX = "TLB-"
}
