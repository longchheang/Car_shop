package com.example.car_shop.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.data.model.Inquiry
import com.example.car_shop.data.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminInquiriesViewModel @Inject constructor(
    private val inquiryRepository: InquiryRepository
) : ViewModel() {

    val inquiries: StateFlow<List<Inquiry>> = inquiryRepository.getAllInquiries()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _uiState = MutableStateFlow(AdminInquiriesUiState())
    val uiState: StateFlow<AdminInquiriesUiState> = _uiState.asStateFlow()

    fun showReplyDialog(inquiry: Inquiry) {
        _uiState.value = _uiState.value.copy(
            selectedInquiry = inquiry,
            replyText = inquiry.reply,
            showReplyDialog = true
        )
    }

    fun hideReplyDialog() {
        _uiState.value = _uiState.value.copy(
            selectedInquiry = null,
            replyText = "",
            showReplyDialog = false,
            replyError = null
        )
    }

    fun onReplyTextChange(text: String) {
        _uiState.value = _uiState.value.copy(replyText = text, replyError = null)
    }

    fun sendReply(onSuccess: () -> Unit) {
        val inquiry = _uiState.value.selectedInquiry ?: return
        val reply = _uiState.value.replyText

        if (reply.isBlank()) {
            _uiState.value = _uiState.value.copy(replyError = "Please enter a reply")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingReply = true, replyError = null)

            inquiryRepository.replyToInquiry(inquiry.id, reply)
                .onSuccess {
                    _uiState.value = AdminInquiriesUiState()
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSendingReply = false,
                        replyError = error.message
                    )
                }
        }
    }
}

data class AdminInquiriesUiState(
    val selectedInquiry: Inquiry? = null,
    val replyText: String = "",
    val showReplyDialog: Boolean = false,
    val isSendingReply: Boolean = false,
    val replyError: String? = null
)
