package com.example.car_shop.ui.admin.inquiries

import com.example.car_shop.data.model.Inquiry

data class AdminInquiriesUiState(
    val selectedInquiry: Inquiry? = null,
    val replyText: String = "",
    val showReplyDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isSendingReply: Boolean = false,
    val isDeleting: Boolean = false,
    val replyError: String? = null
)

