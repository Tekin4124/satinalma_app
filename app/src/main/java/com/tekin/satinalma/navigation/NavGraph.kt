/*
 * NavGraph.kt
 * Jetpack Navigation Compose ile uygulama navigasyon grafiği.
 * Login → Rol'e göre Satınalma / Sevkiyat Ofis / Şoför ekranına yönlendirme.
 */
package com.tekin.satinalma.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tekin.satinalma.domain.model.User
import com.tekin.satinalma.domain.model.UserRole
import com.tekin.satinalma.presentation.auth.LoginScreen
import com.tekin.satinalma.presentation.driver.DriverDetailScreen
import com.tekin.satinalma.presentation.driver.DriverScreen
import com.tekin.satinalma.presentation.logistics.LogisticsDetailScreen
import com.tekin.satinalma.presentation.logistics.LogisticsScreen
import com.tekin.satinalma.presentation.purchasing.PurchasingDetailScreen
import com.tekin.satinalma.presentation.purchasing.PurchasingScreen
import com.tekin.satinalma.util.Constants

/**
 * Uygulama navigasyon grafiği
 *
 * @param navController Navigation controller
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(
        navController = navController,
        startDestination = Constants.Routes.LOGIN
    ) {
        // Giriş ekranı
        composable(Constants.Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    val destination = when (user.role) {
                        UserRole.PURCHASING -> Constants.Routes.PURCHASING
                        UserRole.LOGISTICS_OFFICE -> Constants.Routes.LOGISTICS
                        UserRole.DRIVER -> Constants.Routes.DRIVER
                    }
                    navController.navigate(destination) {
                        popUpTo(Constants.Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Satınalma personeli ekranları
        composable(Constants.Routes.PURCHASING) {
            PurchasingScreen(
                currentUserFullName = currentUser?.fullName ?: "",
                currentUserRole = currentUser?.role?.displayName ?: "",
                onRequestClick = { requestId ->
                    navController.navigate(Constants.Routes.purchasingDetail(requestId))
                },
                onCreateNew = {
                    navController.navigate(Constants.Routes.purchasingDetail(Constants.NEW_REQUEST_ID))
                },
                onLogout = {
                    currentUser = null
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Constants.Routes.PURCHASING_DETAIL,
            arguments = listOf(
                navArgument(Constants.NavArgs.REQUEST_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString(Constants.NavArgs.REQUEST_ID) ?: return@composable
            PurchasingDetailScreen(
                requestId = requestId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Sevkiyat ofis ekranları
        composable(Constants.Routes.LOGISTICS) {
            LogisticsScreen(
                currentUserFullName = currentUser?.fullName ?: "",
                currentUserRole = currentUser?.role?.displayName ?: "",
                onRequestClick = { requestId ->
                    navController.navigate(Constants.Routes.logisticsDetail(requestId))
                },
                onLogout = {
                    currentUser = null
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Constants.Routes.LOGISTICS_DETAIL,
            arguments = listOf(
                navArgument(Constants.NavArgs.REQUEST_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString(Constants.NavArgs.REQUEST_ID) ?: return@composable
            LogisticsDetailScreen(
                requestId = requestId,
                onNavigateBack = { navController.navigateUp() }
            )
        }

        // Şoför ekranları
        composable(Constants.Routes.DRIVER) {
            DriverScreen(
                currentUserFullName = currentUser?.fullName ?: "",
                currentUserRole = currentUser?.role?.displayName ?: "",
                onRequestClick = { requestId ->
                    navController.navigate(Constants.Routes.driverDetail(requestId))
                },
                onLogout = {
                    currentUser = null
                    navController.navigate(Constants.Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Constants.Routes.DRIVER_DETAIL,
            arguments = listOf(
                navArgument(Constants.NavArgs.REQUEST_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString(Constants.NavArgs.REQUEST_ID) ?: return@composable
            DriverDetailScreen(
                requestId = requestId,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}
