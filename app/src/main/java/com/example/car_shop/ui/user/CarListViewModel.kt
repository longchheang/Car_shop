package com.example.car_shop.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.local.FavoritesDataStore
import com.example.car_shop.data.model.Car
import com.example.car_shop.data.repository.AuthRepository
import com.example.car_shop.data.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarListViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val authRepository: AuthRepository,
    private val favoritesDataStore: FavoritesDataStore
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val cars: StateFlow<List<Car>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                carRepository.getAllCars()
            } else {
                carRepository.searchCars(query)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val favorites: StateFlow<Set<String>> = favoritesDataStore.favoritesFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptySet()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(carId: String) {
        viewModelScope.launch {
            if (favorites.value.contains(carId)) {
                favoritesDataStore.removeFavorite(carId)
            } else {
                favoritesDataStore.addFavorite(carId)
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
