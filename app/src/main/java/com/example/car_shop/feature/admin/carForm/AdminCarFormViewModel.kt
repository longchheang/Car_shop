package com.example.car_shop.feature.admin.carForm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.model.Car
import com.example.car_shop.core.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCarFormViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCarFormUiState())
    val uiState: StateFlow<AdminCarFormUiState> = _uiState.asStateFlow()

    fun loadCar(carId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            carRepository.getCarById(carId)
                .onSuccess { car ->
                    _uiState.value = AdminCarFormUiState(
                        isEditMode = true,
                        editingCarId = carId,
                        name = car.name,
                        brand = car.brand,
                        model = car.model,
                        year = car.year.toString(),
                        price = car.price.toString(),
                        description = car.description,
                        mileage = car.mileage.toString(),
                        fuelType = car.fuelType,
                        transmission = car.transmission,
                        existingImageUrl = car.imageUrl,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null, nameError = null)
    }

    fun onBrandChange(brand: String) {
        _uiState.value = _uiState.value.copy(brand = brand, error = null, brandError = null)
    }

    fun onModelChange(model: String) {
        _uiState.value = _uiState.value.copy(model = model, error = null, modelError = null)
    }

    fun onYearChange(year: String) {
        _uiState.value = _uiState.value.copy(year = year, error = null, yearError = null)
    }

    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(price = price, error = null, priceError = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }

    fun onMileageChange(mileage: String) {
        _uiState.value = _uiState.value.copy(mileage = mileage, error = null, mileageError = null)
    }

    fun onFuelTypeChange(fuelType: String) {
        _uiState.value = _uiState.value.copy(fuelType = fuelType, error = null)
    }

    fun onTransmissionChange(transmission: String) {
        _uiState.value = _uiState.value.copy(transmission = transmission, error = null)
    }

    fun onImageSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun saveCar(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Clear all field errors first
        var updatedState = state.copy(
            nameError = null,
            brandError = null,
            modelError = null,
            yearError = null,
            priceError = null,
            mileageError = null,
            error = null
        )

        // Validate required text fields
        var hasError = false

        if (state.name.isBlank()) {
            updatedState = updatedState.copy(nameError = "Car name is required")
            hasError = true
        }

        if (state.brand.isBlank()) {
            updatedState = updatedState.copy(brandError = "Brand is required")
            hasError = true
        }

        if (state.model.isBlank()) {
            updatedState = updatedState.copy(modelError = "Model is required")
            hasError = true
        }

        // Validate year
        val year = state.year.toIntOrNull()
        if (state.year.isBlank()) {
            updatedState = updatedState.copy(yearError = "Year is required")
            hasError = true
        } else if (year == null || year < 1900 || year > 2100) {
            updatedState = updatedState.copy(yearError = "Enter a valid year (1900-2100)")
            hasError = true
        }

        // Validate price
        val price = state.price.toDoubleOrNull()
        if (state.price.isBlank()) {
            updatedState = updatedState.copy(priceError = "Price is required")
            hasError = true
        } else if (price == null || price < 0) {
            updatedState = updatedState.copy(priceError = "Enter a valid price")
            hasError = true
        }

        // Validate mileage
        val mileage = state.mileage.toIntOrNull()
        if (state.mileage.isBlank()) {
            updatedState = updatedState.copy(mileageError = "Mileage is required")
            hasError = true
        } else if (mileage == null || mileage < 0) {
            updatedState = updatedState.copy(mileageError = "Enter a valid mileage")
            hasError = true
        }

        if (hasError) {
            _uiState.value = updatedState
            return
        }

        val car = Car(
            name = state.name,
            brand = state.brand,
            model = state.model,
            year = year!!,
            price = price!!,
            description = state.description,
            mileage = mileage!!,
            fuelType = state.fuelType,
            transmission = state.transmission,
            imageUrl = state.existingImageUrl
        )

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)

            val result = if (state.isEditMode) {
                carRepository.updateCar(state.editingCarId!!, car, state.selectedImageUri)
            } else {
                carRepository.addCar(car, state.selectedImageUri)
            }

            result
                .onSuccess {
                    _uiState.value = state.copy(isSaving = false)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = state.copy(
                        isSaving = false,
                        error = error.message
                    )
                }
        }
    }
}

