package com.example.car_shop.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.model.User
import com.example.car_shop.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
}

