package com.example.car_shop.ui.profile

import com.example.car_shop.data.model.User

data class ProfileUiState(
    val user: User? = null,
    val name: String = "",
    val phone: String = "",
    val isLoading: Boolean = true,
    val isUpdating: Boolean = false,
    val isUploadingImage: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

