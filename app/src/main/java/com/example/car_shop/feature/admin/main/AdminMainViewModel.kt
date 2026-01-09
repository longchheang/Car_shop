package com.example.car_shop.feature.admin.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.car_shop.core.repository.InquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AdminMainViewModel @Inject constructor(
    inquiryRepository: InquiryRepository
) : ViewModel() {

    val unreadInquiriesCount: StateFlow<Int> = inquiryRepository.getAllInquiries()
        .map { list ->
            list.count { !it.adminHasRead }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
}
