package com.iqqi.ime.keyboard.state

import androidx.compose.runtime.staticCompositionLocalOf
import com.iqqi.ime.keyboard.model.KeyboardLanguage
import com.iqqi.ime.keyboard.model.KeyboardThemeSpec

val localKeyboardStyle = staticCompositionLocalOf<KeyboardThemeSpec> {
    error("No KeyboardStyle provided")
}

data class KeyboardState(
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val isShift: Boolean = false,
    val isSymbol: Boolean = false
)