package com.example.car_shop.feature.profile

import androidx.lifecycle.ViewModel
import com.example.car_shop.core.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapPickerViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    suspend fun saveLocation(location: String) {
        settingsRepository.setShopLocation(location)
    }
}
