package com.example.car_shop.ui.user.favorites

import com.example.car_shop.data.model.Car

data class FavoritesUiState(
    val cars: List<Car> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

