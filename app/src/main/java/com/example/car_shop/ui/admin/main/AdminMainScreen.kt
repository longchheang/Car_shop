package com.example.car_shop.ui.admin.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.car_shop.navigation.BottomNavItem
import com.example.car_shop.navigation.Screen
import com.example.car_shop.ui.admin.dashboard.AdminDashboardScreen
import com.example.car_shop.ui.admin.inquiries.AdminInquiriesScreen
import com.example.car_shop.ui.profile.ProfileScreen

@Composable
fun AdminMainScreen(
    onAddCar: () -> Unit,
    onEditCar: (String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.AdminCars,
        BottomNavItem.AdminInquiries,
        BottomNavItem.AdminProfile
    )

    Scaffold(
        // Handle only the bottom system inset here; inner screens handle top
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom),
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.AdminDashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    onAddCar = onAddCar,
                    onEditCar = onEditCar
                )
            }
            composable(Screen.AdminInquiries.route) {
                AdminInquiriesScreen()
            }
            composable(Screen.Profile.route) {
                // Reusing the User Profile Screen, assuming it works for Admin as well.
                ProfileScreen(
                    onEditProfile = onEditProfile,
                    onLogout = onLogout
                )
            }
        }
    }
}
