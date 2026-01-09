package com.example.car_shop.feature.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.model.Car
import com.example.car_shop.core.repository.AuthRepository
import com.example.car_shop.core.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedYear = MutableStateFlow<Int?>(null)
    val selectedYear: StateFlow<Int?> = _selectedYear.asStateFlow()

    // Single source of truth for all cars
    val allCars: StateFlow<List<Car>> = carRepository.getAllCars()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    
    // Computed available years
    val availableYears: StateFlow<List<String>> = allCars
        .map { cars ->
            val years = cars.map { it.year }.distinct().sortedDescending().map { it.toString() }
            listOf("All") + years
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            listOf("All")
        )

    // Filtered and sorted cars
    val cars: StateFlow<List<Car>> = combine(
        allCars,
        _searchQuery,
        _selectedYear
    ) { cars, query, year ->
        var result = cars

        // Filter by search query
        if (query.isNotBlank()) {
            result = result.filter { car ->
                car.name.contains(query, ignoreCase = true) ||
                car.brand.contains(query, ignoreCase = true) ||
                car.model.contains(query, ignoreCase = true)
            }
        }

        // Filter by year
        if (year != null) {
            result = result.filter { it.year == year }
        }

        // Sort by year descending (newest to oldest)
        result.sortedByDescending { it.year }
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        // Load total user count for dashboard stats
        viewModelScope.launch {
            authRepository.getTotalUsers()
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(totalUsers = count)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

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

    fun toggleCarAvailability(car: Car) {
        viewModelScope.launch {
            val updatedCar = car.copy(isAvailable = !car.isAvailable)
            carRepository.updateCar(car.id, updatedCar, null)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onYearSelected(year: String) {
        _selectedYear.value = if (year == "All") null else year.toIntOrNull()
    }

}

data class AdminDashboardUiState(
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val totalUsers: Int = 0,
    val error: String? = null,
    val isUpdatingAvailability: Boolean = false
)
