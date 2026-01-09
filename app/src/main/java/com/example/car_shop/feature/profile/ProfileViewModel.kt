package com.example.car_shop.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.model.User
import com.example.car_shop.core.repository.AuthRepository
import com.example.car_shop.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            authRepository.getCurrentUser()
                .onSuccess { user ->
                    if (user != null) {
                        _uiState.value = ProfileUiState(
                            user = user,
                            name = user.name,
                            phone = user.phone,
                            isLoading = false
                        )
                        // Load shop location if admin
                        if (user.isAdmin) {
                            loadShopLocation()
                        }
                    }
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
        _uiState.value = _uiState.value.copy(name = name, error = null, successMessage = null)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone, error = null, successMessage = null)
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingImage = true, error = null)

            authRepository.uploadProfileImage(uri)
                .onSuccess { downloadUrl ->
                    // Update user profile with new image URL
                    authRepository.updateProfile(
                        name = uiState.value.name,
                        phone = uiState.value.phone,
                        imageUrl = downloadUrl
                    ).onSuccess {
                        _uiState.value = _uiState.value.copy(isUploadingImage = false)
                        loadProfile()
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isUploadingImage = false,
                            error = error.message
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUploadingImage = false,
                        error = error.message
                    )
                }
        }
    }

    fun updateProfile() {
        val name = _uiState.value.name
        val phone = _uiState.value.phone

        if (name.isBlank() || phone.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpdating = true, error = null)

            authRepository.updateProfile(name, phone)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        successMessage = "Profile updated successfully"
                    )
                    loadProfile()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isUpdating = false,
                        error = error.message
                    )
                }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    // Shop location management (admin only)
    private fun loadShopLocation() {
        viewModelScope.launch {
            settingsRepository.getShopLocation()
                .onSuccess { location ->
                    _uiState.value = _uiState.value.copy(
                        shopLocation = location,
                        locationInput = location
                    )
                }
        }
    }

    fun showLocationDialog() {
        _uiState.value = _uiState.value.copy(
            showLocationDialog = true,
            locationInput = _uiState.value.shopLocation
        )
    }

    fun hideLocationDialog() {
        _uiState.value = _uiState.value.copy(showLocationDialog = false)
    }

    fun onLocationInputChange(location: String) {
        _uiState.value = _uiState.value.copy(locationInput = location)
    }

    fun saveShopLocation() {
        val location = _uiState.value.locationInput
        if (location.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a location")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingLocation = true)

            settingsRepository.setShopLocation(location)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        shopLocation = location,
                        showLocationDialog = false,
                        isSavingLocation = false,
                        successMessage = "Location saved successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSavingLocation = false,
                        error = error.message
                    )
                }
        }
    }
}

