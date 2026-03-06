package com.iqqi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.iqqi.datastore.PreferencesKeys
import com.iqqi.datastore.dataStore
import com.iqqi.settings.BackgroundImage
import com.iqqi.settings.CandidateHeight
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.ThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    val enableDigitalFlow: Flow<Boolean> =
        context.dataStore.data.map {
            it[PreferencesKeys.ENABLE_Digital] ?: true
        }

    suspend fun setEnableDigital(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.ENABLE_Digital] = enabled
        }
    }

    val keyboardHeightFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.KEYBOARD_HEIGHT] ?: KeyboardHeight.MEDIUM.name
        runCatching {
            KeyboardHeight.valueOf(stored)
        }.getOrDefault(KeyboardHeight.MEDIUM)
    }


    suspend fun setKeyboardHeight(height: KeyboardHeight) {
        context.dataStore.edit {
            it[PreferencesKeys.KEYBOARD_HEIGHT] = height.name
        }
    }

    val candidateHeightFlow = context.dataStore.data.map {
        it[PreferencesKeys.CANDIDATE_HEIGHT] ?: CandidateHeight.MEDIUM.scale
    }

    suspend fun setCandidateHeight(height: CandidateHeight) {
        context.dataStore.edit {
            it[PreferencesKeys.CANDIDATE_HEIGHT] = height.scale
        }
    }

    val themeColorFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.THEME_COLOR] ?: ThemeColor.WHITE.name
        runCatching {
            ThemeColor.valueOf(stored)
        }.getOrDefault(ThemeColor.WHITE)
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME_COLOR] = color.name
        }
    }

    val keyboardBackgroundImageFlow = context.dataStore.data.map {
        val stored = it[PreferencesKeys.KEYBOARD_BACKGROUND] ?: BackgroundImage.NONE.name
        runCatching {
            BackgroundImage.valueOf(stored)
        }.getOrDefault(BackgroundImage.NONE)
    }

    suspend fun setKeyboardBackgroundImage(image: BackgroundImage) {
        context.dataStore.edit {
            it[PreferencesKeys.KEYBOARD_BACKGROUND] = image.name
        }
    }
}