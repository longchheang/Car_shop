package com.example.car_shop.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object UserHome : BottomNavItem(
        route = Screen.CarList.route,
        title = "Home",
        icon = Icons.Default.Home
    )
    object UserFavorites : BottomNavItem(
        route = Screen.Favorites.route,
        title = "Favorites",
        icon = Icons.Default.Favorite
    )
    object UserProfile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profile",
        icon = Icons.Default.Person
    )

    object AdminCars : BottomNavItem(
        route = Screen.AdminDashboard.route,
        title = "Cars",
        icon = Icons.AutoMirrored.Filled.List
    )
    object AdminInquiries : BottomNavItem(
        route = Screen.AdminInquiries.route,
        title = "Inquiries",
        icon = Icons.Default.Email
    )
    object AdminProfile : BottomNavItem(
        route = Screen.Profile.route, // Reusing profile route? Or should it be separate?
        // Using the same route "profile" in a separate nested graph is fine.
        title = "Profile",
        icon = Icons.Default.Person
    )
}

