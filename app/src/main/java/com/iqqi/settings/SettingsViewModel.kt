package com.iqqi.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqqi.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val enableDigital = repository.enableDigitalFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            repository.defaultSetting.enableDigital
        )

    fun toggleDigital(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnableDigital(enabled)
        }
    }

    val currentKeyboardHeight = repository.keyboardHeightFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultSetting.keyboardHeight
    )

    fun setKeyboardHeight(keyboardHeight: KeyboardHeight) {
        viewModelScope.launch {
            repository.setKeyboardHeight(keyboardHeight)
        }
    }

    val currentCandidateHeight = repository.candidateHeightFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultSetting.candidateHeight
    )

    fun setCandidateHeight(candidateHeight: CandidateHeight) {
        viewModelScope.launch {
            repository.setCandidateHeight(candidateHeight)
        }
    }


    val themeColor = repository.themeColorFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultSetting.themeColor
    )

    fun setThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            repository.setThemeColor(color)
        }
    }


    val keyboardBackgroundImage = repository.keyboardBackgroundImageFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultSetting.backgroundImage
    )

    fun setKeyboardBackgroundImage(image: BackgroundImage) {
        viewModelScope.launch {
            repository.setKeyboardBackgroundImage(image)
        }
    }
}