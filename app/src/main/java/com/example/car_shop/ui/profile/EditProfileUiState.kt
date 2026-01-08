package com.example.car_shop.ui.profile

import com.example.car_shop.data.model.User

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val password: String = "",
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

