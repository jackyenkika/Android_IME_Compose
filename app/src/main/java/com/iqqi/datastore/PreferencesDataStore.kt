package com.iqqi.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore("ime_settings")

object PreferencesKeys {
    val ENABLE_Digital = booleanPreferencesKey("enable_digital")
    val KEYBOARD_HEIGHT = floatPreferencesKey("keyboard_height")
    val CANDIDATE_HEIGHT = floatPreferencesKey("candidate_height")


    val THEME_COLOR = stringPreferencesKey("theme_color")

    val KEYBOARD_BACKGROUND = stringPreferencesKey("keyboard_background")

}
