package com.iqqi.settings

import com.iqqi.data.LanguageRepository
import com.iqqi.data.SettingsRepository

class SettingsViewModelFactory(
    private val settingRepository: SettingsRepository,
    private val languageRepository: LanguageRepository
) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingRepository, languageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}