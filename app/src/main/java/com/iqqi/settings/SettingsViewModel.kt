package com.iqqi.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iqqi.data.LanguageRepository
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.util.LogObj
import com.iqqi.keyboard.model.ImeLanguage
import com.iqqi.settings.ui.theme.FontType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val languageRepository: LanguageRepository
) : ViewModel() {

    private val _availableLanguages = MutableStateFlow<List<ImeLanguage>>(emptyList())
    val availableLanguages: StateFlow<List<ImeLanguage>> = _availableLanguages

    init {
        viewModelScope.launch {
            // 讀取目前已啟用語言
            val enabledLocales = repository.enabledLanguagesFlow.first()

            // 第一次安裝，沒有任何語言時
            val allLanguages = languageRepository.getAvailableLanguages()
            val defaultLocales = allLanguages.mapNotNull { it.locale }.toSet()

            LogObj.trace("allLanguages = $allLanguages , defaultLocales= $defaultLocales , enabledLocales = $enabledLocales")
            if (enabledLocales.isEmpty()) {
                repository.setEnabledLanguages(defaultLocales)
                // 直接更新 _availableLanguages
                _availableLanguages.value = allLanguages.map { lang ->
                    lang.copy(enabled = lang.locale in defaultLocales)
                }
            } else {
                _availableLanguages.value = allLanguages.map { lang ->
                    lang.copy(enabled = lang.locale in enabledLocales)
                }
            }
        }

        // 監聽 DataStore 的變化
        viewModelScope.launch {
            repository.enabledLanguagesFlow.collect { enabledLocales ->
                val allLanguages = languageRepository.getAvailableLanguages()
                _availableLanguages.value = allLanguages.map { lang ->
                    lang.copy(enabled = lang.locale in enabledLocales)
                }
            }
        }
    }

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

    val fontType = repository.fontTypeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.defaultSetting.fontType
    )

    fun setFontType(font: FontType) {
        viewModelScope.launch {
            repository.setFontType(font)
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


    // 切換啟用狀態
    fun toggleLanguage(language: ImeLanguage, enabled: Boolean) {
        viewModelScope.launch {
            // 更新 DataStore
            repository.toggleLanguage(language.locale ?: return@launch, enabled)
            // _availableLanguages 會自動在 collect repository.enabledLanguagesFlow 中刷新
        }
    }
}