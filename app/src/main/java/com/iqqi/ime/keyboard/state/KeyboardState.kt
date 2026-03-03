package com.iqqi.ime.keyboard.state

import androidx.compose.runtime.staticCompositionLocalOf
import com.iqqi.ime.keyboard.model.KeyboardThemeSpec


val localKeyboardStyle = staticCompositionLocalOf<KeyboardThemeSpec> {
    error("No KeyboardStyle provided")
}