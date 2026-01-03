package com.example.car_shop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.model.Car
import com.example.car_shop.data.repository.AuthRepository
import com.example.car_shop.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val cars: StateFlow<List<Car>> = carRepository.getAllCars()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    fun deleteCar(carId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            carRepository.deleteCar(carId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isDeleting = false,
                        error = error.message
                    )
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}

data class AdminDashboardUiState(
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)
