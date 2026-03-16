package com.iqqi

import android.content.Context
import com.iqqi.data.SettingsRepository
import com.iqqi.data.StickerRepository

class AppContainer(context: Context) {

    val settingsRepository by lazy {
        SettingsRepository(context)
    }

    val stickerRepository by lazy {
        StickerRepository(context)
    }
}