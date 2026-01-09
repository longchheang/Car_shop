package com.example.car_shop.feature.profile

import com.example.car_shop.core.model.User

data class ProfileUiState(
    val user: User? = null,
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    // Shop location for admin
    val shopLocation: String = "",
    val showLocationDialog: Boolean = false,
    val locationInput: String = "",
    val isSavingLocation: Boolean = false
)

