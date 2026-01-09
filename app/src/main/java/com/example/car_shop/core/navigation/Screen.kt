package com.example.car_shop.core.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")

    object Login : Screen("login")
    object Register : Screen("register")
    object CarList : Screen("car_list")
    object CarDetail : Screen("car_detail/{carId}") {
        fun createRoute(carId: String) = "car_detail/$carId"
    }
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object UserInquiries : Screen("user_inquiries")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminCarForm : Screen("admin_car_form?carId={carId}") {
        fun createRoute(carId: String? = null) = if (carId != null) "admin_car_form?carId=$carId" else "admin_car_form"
    }
    object AdminInquiries : Screen("admin_inquiries")
    object EditProfile : Screen("edit_profile")
    object MapPicker : Screen("map_picker?location={location}") {
        fun createRoute(location: String = "") = "map_picker?location=$location"
    }

    // Main Container Screens
    object UserMain : Screen("user_main")
    object AdminMain : Screen("admin_main")
}

