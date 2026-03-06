package com.iqqi.core

import android.content.Context
import com.iqqi.data.SettingsRepository

class AppContainer(context: Context) {

    val settingsRepository by lazy {
        SettingsRepository(context)
    }
}