package com.example.car_shop.feature.user.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.local.FavoritesDataStore
import com.example.car_shop.core.model.Car
import com.example.car_shop.core.repository.CarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val favoritesDataStore: FavoritesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoritesDataStore.favoritesFlow.collect { favoriteIds ->
                if (favoriteIds.isEmpty()) {
                    _uiState.value = FavoritesUiState(cars = emptyList(), isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    carRepository.getCarsByIds(favoriteIds)
                        .onSuccess { cars ->
                            _uiState.value = FavoritesUiState(cars = cars, isLoading = false)
                        }
                        .onFailure { error ->
                            _uiState.value = FavoritesUiState(
                                cars = emptyList(),
                                isLoading = false,
                                error = error.message
                            )
                        }
                }
            }
        }
    }

    fun removeFavorite(carId: String) {
        viewModelScope.launch {
            favoritesDataStore.removeFavorite(carId)
        }
    }
}

