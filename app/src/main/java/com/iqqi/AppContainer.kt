package com.iqqi

import android.content.Context
import android.view.inputmethod.InputMethodManager
import com.iqqi.data.LanguageRepository
import com.iqqi.data.SettingsRepository
import com.iqqi.data.StickerRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


    val settingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val stickerRepository by lazy {
        StickerRepository(appContext)
    }

    val languageRepository: LanguageRepository by lazy {
        LanguageRepository(imm = imm, packageName = appContext.packageName)
    }
}