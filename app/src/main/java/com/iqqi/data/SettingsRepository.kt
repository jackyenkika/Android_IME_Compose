package com.iqqi.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.iqqi.datastore.PreferencesKeys
import com.iqqi.datastore.PreferencesKeys.CANDIDATE_HEIGHT
import com.iqqi.datastore.PreferencesKeys.KEYBOARD_HEIGHT
import com.iqqi.datastore.dataStore
import com.iqqi.settings.CandidateHeight
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.ThemeColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    val enableSoundFlow: Flow<Boolean> =
        context.dataStore.data.map {
            it[PreferencesKeys.ENABLE_SOUND] ?: true
        }

    suspend fun setEnableSound(enabled: Boolean) {
        context.dataStore.edit {
            it[PreferencesKeys.ENABLE_SOUND] = enabled
        }
    }

    val keyboardHeightFlow = context.dataStore.data.map {
        it[KEYBOARD_HEIGHT] ?: KeyboardHeight.MEDIUM.scale
    }

    suspend fun setKeyboardHeight(height: KeyboardHeight) {
        context.dataStore.edit {
            it[KEYBOARD_HEIGHT] = height.scale
        }
    }

    val candidateHeightFlow = context.dataStore.data.map {
        it[CANDIDATE_HEIGHT] ?: CandidateHeight.MEDIUM.scale
    }

    suspend fun setCandidateHeight(height: CandidateHeight) {
        context.dataStore.edit {
            it[CANDIDATE_HEIGHT] = height.scale
        }
    }

    val themeColorFlow = context.dataStore.data.map {
        ThemeColor.valueOf(
            it[PreferencesKeys.THEME_COLOR] ?: ThemeColor.BLUE.name
        )
    }

    suspend fun setThemeColor(color: ThemeColor) {
        context.dataStore.edit {
            it[PreferencesKeys.THEME_COLOR] = color.name
        }
    }
}