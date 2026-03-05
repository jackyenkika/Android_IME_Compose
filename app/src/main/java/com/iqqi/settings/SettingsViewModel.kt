package com.iqqi.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqqi.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository
) : ViewModel() {

    val enableDigital = repository.enableDigitalFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            true
        )

    fun toggleDigital(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnableDigital(enabled)
        }
    }

    val currentKeyboardHeight = repository.keyboardHeightFlow
        .map { scale ->
            KeyboardHeight.entries.find { it.scale == scale }
                ?: KeyboardHeight.MEDIUM
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            KeyboardHeight.MEDIUM
        )

    fun setKeyboardHeight(keyboardHeight: KeyboardHeight) {
        viewModelScope.launch {
            repository.setKeyboardHeight(keyboardHeight)
        }
    }

    val currentCandidateHeight = repository.candidateHeightFlow
        .map { scale ->
            CandidateHeight.entries.find { it.scale == scale }
                ?: CandidateHeight.MEDIUM
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CandidateHeight.MEDIUM
        )

    fun setCandidateHeight(candidateHeight: CandidateHeight) {
        viewModelScope.launch {
            repository.setCandidateHeight(candidateHeight)
        }
    }


    val themeColor = repository.themeColorFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ThemeColor.BLUE
    )

    fun setThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            repository.setThemeColor(color)
        }
    }
}