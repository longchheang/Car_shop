package com.example.car_shop.ui.admin.carForm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.model.Car
import com.example.car_shop.data.repository.CarRepository
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
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun onBrandChange(brand: String) {
        _uiState.value = _uiState.value.copy(brand = brand, error = null)
    }

    fun onModelChange(model: String) {
        _uiState.value = _uiState.value.copy(model = model, error = null)
    }

    fun onYearChange(year: String) {
        _uiState.value = _uiState.value.copy(year = year, error = null)
    }

    fun onPriceChange(price: String) {
        _uiState.value = _uiState.value.copy(price = price, error = null)
    }

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description, error = null)
    }

    fun onMileageChange(mileage: String) {
        _uiState.value = _uiState.value.copy(mileage = mileage, error = null)
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

        // Validation
        if (state.name.isBlank() || state.brand.isBlank() || state.model.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        val year = state.year.toIntOrNull()
        val price = state.price.toDoubleOrNull()
        val mileage = state.mileage.toIntOrNull()

        if (year == null || price == null || mileage == null) {
            _uiState.value = state.copy(error = "Please enter valid numbers")
            return
        }

        val car = Car(
            name = state.name,
            brand = state.brand,
            model = state.model,
            year = year,
            price = price,
            description = state.description,
            mileage = mileage,
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

