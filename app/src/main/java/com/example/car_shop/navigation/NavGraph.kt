package com.example.car_shop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.car_shop.ui.admin.carForm.AdminCarFormScreen
import com.example.car_shop.ui.admin.dashboard.AdminDashboardScreen
import com.example.car_shop.ui.admin.inquiries.AdminInquiriesScreen
import com.example.car_shop.ui.auth.login.LoginScreen
import com.example.car_shop.ui.auth.register.RegisterScreen
import com.example.car_shop.ui.user.detail.CarDetailScreen
import com.example.car_shop.ui.user.list.CarListScreen
import com.example.car_shop.ui.user.favorites.FavoritesScreen
import com.example.car_shop.ui.user.profile.ProfileScreen


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { isAdmin ->
                    navController.navigate(
                        if (isAdmin) Screen.AdminDashboard.route else Screen.CarList.route
                    ) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.CarList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // User Screens
        composable(Screen.CarList.route) {
            CarListScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onFavoritesClick = {
                    navController.navigate(Screen.Favorites.route)
                },
                onProfileClick = {
                    navController.navigate(Screen.Profile.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.CarDetail.route,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailScreen(
                carId = carId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Admin Screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onAddCar = {
                    navController.navigate(Screen.AdminCarForm.createRoute())
                },
                onEditCar = { carId ->
                    navController.navigate(Screen.AdminCarForm.createRoute(carId))
                },
                onViewInquiries = {
                    navController.navigate(Screen.AdminInquiries.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.AdminCarForm.route,
            arguments = listOf(navArgument("carId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")
            AdminCarFormScreen(
                carId = carId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdminInquiries.route) {
            AdminInquiriesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
