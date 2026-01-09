package com.example.car_shop.feature.admin.carForm

import android.net.Uri

data class AdminCarFormUiState(
    val isEditMode: Boolean = false,
    val editingCarId: String? = null,
    val name: String = "",
    val brand: String = "",
    val model: String = "",
    val year: String = "",
    val price: String = "",
    val description: String = "",
    val mileage: String = "",
    val fuelType: String = "Petrol",
    val transmission: String = "Manual",
    val selectedImageUri: Uri? = null,
    val existingImageUrl: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // Field-specific validation errors
    val nameError: String? = null,
    val brandError: String? = null,
    val modelError: String? = null,
    val yearError: String? = null,
    val priceError: String? = null,
    val mileageError: String? = null
)

