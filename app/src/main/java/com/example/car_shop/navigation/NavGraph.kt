package com.example.car_shop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.car_shop.ui.admin.carForm.AdminCarFormScreen
import com.example.car_shop.ui.admin.inquiries.AdminInquiriesScreen
import com.example.car_shop.ui.auth.login.LoginScreen
import com.example.car_shop.ui.auth.register.RegisterScreen
import com.example.car_shop.ui.onboarding.OnboardingScreen
import com.example.car_shop.ui.user.detail.CarDetailScreen
import com.example.car_shop.ui.profile.EditProfileScreen


@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash
        composable(Screen.Splash.route) {
            com.example.car_shop.ui.splash.SplashScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

      //Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }

        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = { isAdmin ->
                    navController.navigate(
                        if (isAdmin) Screen.AdminMain.route else Screen.UserMain.route
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
                    navController.navigate(Screen.UserMain.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main User Screen (Containers)
        composable(Screen.UserMain.route) {
            com.example.car_shop.ui.user.main.UserMainScreen(
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onEditProfile = {
                   navController.navigate(Screen.EditProfile.route)
                },
                onSeeHelp = {
                    navController.navigate(Screen.UserInquiries.route)
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Main Admin Screen (Containers)
        composable(Screen.AdminMain.route) {
            com.example.car_shop.ui.admin.main.AdminMainScreen(
                onAddCar = {
                    navController.navigate(Screen.AdminCarForm.createRoute())
                },
                onEditCar = { carId ->
                    navController.navigate(Screen.AdminCarForm.createRoute(carId))
                },
                onCarClick = { carId ->
                    navController.navigate(Screen.CarDetail.createRoute(carId))
                },
                onEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
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
            AdminInquiriesScreen()
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.UserInquiries.route) {
            com.example.car_shop.ui.user.inquiries.UserInquiriesScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
