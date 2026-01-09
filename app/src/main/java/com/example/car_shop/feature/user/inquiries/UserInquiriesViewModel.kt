package com.example.car_shop.feature.user.inquiries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.model.Inquiry
import com.example.car_shop.core.repository.AuthRepository
import com.example.car_shop.core.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserInquiriesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    private val _inquiries = MutableStateFlow<List<Inquiry>>(emptyList())
    val inquiries: StateFlow<List<Inquiry>> = _inquiries.asStateFlow()

    private val _isHiding = MutableStateFlow(false)
    val isHiding: StateFlow<Boolean> = _isHiding.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authRepository.currentUserId
            val flow = if (userId != null) {
                inquiryRepository.getUserInquiries(userId)
            } else {
                emptyFlow()
            }

            flow.collect { list ->
                // Filter out inquiries that are hidden by the user
                _inquiries.value = list.filter { !it.hiddenByUser }
            }
        }
        
        markMessagesAsRead()
    }

    fun markMessagesAsRead() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId
            if (userId != null) {
                inquiryRepository.markInquiriesAsReadByUser(userId)
            }
        }
    }

    fun hideInquiry(inquiryId: String) {
        if (_isHiding.value) return
        viewModelScope.launch {
            _isHiding.value = true
            _error.value = null

            inquiryRepository.hideInquiry(inquiryId)
                .onSuccess {
                    _isHiding.value = false
                }
                .onFailure { e ->
                    _isHiding.value = false
                    _error.value = e.message
                }
        }
    }

    fun hideAllInquiries() {
        if (_isHiding.value) return
        viewModelScope.launch {
            _isHiding.value = true
            _error.value = null

            val userId = authRepository.currentUserId
            if (userId != null) {
                inquiryRepository.hideAllUserInquiries(userId)
                    .onSuccess {
                        _isHiding.value = false
                    }
                    .onFailure { e ->
                        _isHiding.value = false
                        _error.value = e.message
                    }
            } else {
                _isHiding.value = false
                _error.value = "User not logged in"
            }
        }
    }
}


