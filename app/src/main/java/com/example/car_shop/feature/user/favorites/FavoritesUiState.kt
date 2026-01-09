package com.example.car_shop.feature.user.favorites

import com.example.car_shop.core.model.Car

data class FavoritesUiState(
    val cars: List<Car> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

