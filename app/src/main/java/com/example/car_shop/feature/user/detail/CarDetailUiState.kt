package com.example.car_shop.feature.user.detail

import com.example.car_shop.core.model.Car

data class CarDetailUiState(
    val car: Car? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showInquiryDialog: Boolean = false,
    val inquiryMessage: String = "",
    val isSendingInquiry: Boolean = false,
    val inquiryError: String? = null
)

