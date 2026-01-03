package com.example.car_shop.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.local.FavoritesDataStore
import com.example.car_shop.data.model.Car
import com.example.car_shop.data.model.Inquiry
import com.example.car_shop.data.repository.AuthRepository
import com.example.car_shop.data.repository.CarRepository
import com.example.car_shop.data.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val authRepository: AuthRepository,
    private val inquiryRepository: InquiryRepository,
    private val favoritesDataStore: FavoritesDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState: StateFlow<CarDetailUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    fun loadCar(carId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            carRepository.getCarById(carId)
                .onSuccess { car ->
                    _uiState.value = _uiState.value.copy(
                        car = car,
                        isLoading = false
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }

            // Check if favorite
            favoritesDataStore.favoritesFlow.collect { favorites ->
                _isFavorite.value = favorites.contains(carId)
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val carId = _uiState.value.car?.id ?: return@launch
            if (_isFavorite.value) {
                favoritesDataStore.removeFavorite(carId)
            } else {
                favoritesDataStore.addFavorite(carId)
            }
        }
    }

    fun onMessageChange(message: String) {
        _uiState.value = _uiState.value.copy(inquiryMessage = message, inquiryError = null)
    }

    fun sendInquiry(onSuccess: () -> Unit) {
        val car = _uiState.value.car ?: return
        val message = _uiState.value.inquiryMessage

        if (message.isBlank()) {
            _uiState.value = _uiState.value.copy(inquiryError = "Please enter a message")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingInquiry = true, inquiryError = null)

            val currentUser = authRepository.getCurrentUser().getOrNull()
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isSendingInquiry = false,
                    inquiryError = "User not found"
                )
                return@launch
            }

            val inquiry = Inquiry(
                carId = car.id,
                carName = car.name,
                userId = currentUser.id,
                userName = currentUser.name,
                userEmail = currentUser.email,
                userPhone = currentUser.phone,
                message = message
            )

            inquiryRepository.sendInquiry(inquiry)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSendingInquiry = false,
                        inquiryMessage = "",
                        showInquiryDialog = false
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingInquiry = false,
                        inquiryError = error.message
                    )
                }
        }
    }

    fun showInquiryDialog() {
        _uiState.value = _uiState.value.copy(showInquiryDialog = true)
    }

    fun hideInquiryDialog() {
        _uiState.value = _uiState.value.copy(
            showInquiryDialog = false,
            inquiryMessage = "",
            inquiryError = null
        )
    }
}

data class CarDetailUiState(
    val car: Car? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showInquiryDialog: Boolean = false,
    val inquiryMessage: String = "",
    val isSendingInquiry: Boolean = false,
    val inquiryError: String? = null
)
