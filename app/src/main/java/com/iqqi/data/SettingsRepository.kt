package com.iqqi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.iqqi.datastore.PreferencesKeys
import com.iqqi.datastore.dataStore
import com.iqqi.keyboard.model.ImeLanguage
import com.iqqi.settings.BackgroundImage
import com.iqqi.settings.CandidateHeight
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.ThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    val defaultSetting = DefaultSetting()

    val enableDigitalFlow: Flow<Boolean> =
        context.dataStore.data.map {
            it[PreferencesKeys.ENABLE_Digital] ?: defaultSetting.enableDigital
        }

    suspend fun setEnableDigital(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.ENABLE_Digital] = enabled
        }
    }

    val keyboardHeightFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.KEYBOARD_HEIGHT] ?: defaultSetting.keyboardHeight.name
        runCatching {
            KeyboardHeight.valueOf(stored)
        }.getOrDefault(defaultSetting.keyboardHeight)
    }


    suspend fun setKeyboardHeight(height: KeyboardHeight) {
        context.dataStore.edit {
            it[PreferencesKeys.KEYBOARD_HEIGHT] = height.name
        }
    }

    val candidateHeightFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.CANDIDATE_HEIGHT] ?: defaultSetting.candidateHeight.name
        runCatching {
            CandidateHeight.valueOf(stored)
        }.getOrDefault(defaultSetting.candidateHeight)
    }

    suspend fun setCandidateHeight(height: CandidateHeight) {
        context.dataStore.edit {
            it[PreferencesKeys.CANDIDATE_HEIGHT] = height.name
        }
    }

    val themeColorFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.THEME_COLOR] ?: defaultSetting.themeColor.name
        runCatching {
            ThemeColor.valueOf(stored)
        }.getOrDefault(defaultSetting.themeColor)
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME_COLOR] = color.name
        }
    }

    val keyboardBackgroundImageFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.KEYBOARD_BACKGROUND] ?: defaultSetting.backgroundImage.name
        runCatching {
            BackgroundImage.valueOf(stored)
        }.getOrDefault(defaultSetting.backgroundImage)
    }

    suspend fun setKeyboardBackgroundImage(image: BackgroundImage) {
        context.dataStore.edit {
            it[PreferencesKeys.KEYBOARD_BACKGROUND] = image.name
        }
    }

    // 1. 讀取已啟用語言
    val enabledLanguagesFlow: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            val stored = prefs[PreferencesKeys.ENABLED_LANGUAGES]
            if (stored.isNullOrEmpty()) {
                // DataStore 還沒設定，使用 defaultSetting.defaultLanguage
                defaultSetting.defaultLanguage.mapNotNull { it.locale }.toSet()
            } else {
                stored.split(",").toSet()
            }
        }

    // 2. 更新啟用語言
    suspend fun setEnabledLanguages(locales: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ENABLED_LANGUAGES] = locales.joinToString(",")
        }
    }

    // 3. 單一語言開關
    suspend fun toggleLanguage(locale: String, enabled: Boolean) {
        val current = enabledLanguagesFlow.first() // 讀取當前啟用語言
        val updated = if (enabled) current + locale else current - locale
        setEnabledLanguages(updated)
    }
}

data class DefaultSetting(
    val enableDigital: Boolean = true,

    val keyboardHeight: KeyboardHeight = KeyboardHeight.MEDIUM,
    val candidateHeight: CandidateHeight = CandidateHeight.MEDIUM,

    val themeColor: ThemeColor = ThemeColor.WHITE,
    val backgroundImage: BackgroundImage = BackgroundImage.FIFA_GREEN,

    val defaultLanguage: List<ImeLanguage> = listOf(ImeLanguage())
)