package com.example.car_shop.ui.user.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.local.FavoritesDataStore
import com.example.car_shop.data.model.Car
import com.example.car_shop.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarListViewModel @Inject constructor(
    carRepository: CarRepository,
    private val favoritesDataStore: FavoritesDataStore
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

        // Always sort by year descending (newest to oldest) as requested
        result.sortedByDescending { it.year }
    }
    .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onYearSelected(year: String) {
        _selectedYear.value = if (year == "All") null else year.toIntOrNull()
    }

    val favorites: StateFlow<Set<String>> = favoritesDataStore.favoritesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptySet()
        )

    fun toggleFavorite(carId: String) {
        viewModelScope.launch {
            if (favorites.value.contains(carId)) {
                favoritesDataStore.removeFavorite(carId)
            } else {
                favoritesDataStore.addFavorite(carId)
            }
        }
    }

}


