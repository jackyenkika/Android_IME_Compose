package com.iqqi.keyboard.model

import android.view.inputmethod.InputMethodSubtype

data class ImeLanguage(
    val name: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val locale: String? = "en_US",
    val subtype: InputMethodSubtype? = null,
    val enabled: Boolean = false
)

enum class KeyboardLanguage {
    ENGLISH,
    CHINESE,
    JAPANESE
}