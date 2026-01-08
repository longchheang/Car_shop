package com.example.car_shop.ui.profile

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
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.getCurrentUser().onSuccess { user ->
                user?.let {
                    _uiState.value = _uiState.value.copy(
                        name = it.name,
                        phone = it.phone,
                        currentUser = it,
                        isLoading = false
                    )
                }
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile"
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name)
    }

    fun onPhoneChange(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun saveChanges() {
        val state = _uiState.value
        if (state.currentUser == null) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)

            // 1. Verify Password by logging in again
            authRepository.login(state.currentUser.email, state.password)
                .onSuccess {
                    // 2. If verified, update profile
                    authRepository.updateProfile(state.name, state.phone)
                        .onSuccess {
                            _uiState.value = state.copy(isSaving = false, isSuccess = true)
                        }
                        .onFailure { error ->
                            _uiState.value = state.copy(isSaving = false, error = "Update failed: ${error.message}")
                        }
                }
                .onFailure {
                    _uiState.value = state.copy(isSaving = false, error = "Incorrect password")
                }
        }
    }
}

