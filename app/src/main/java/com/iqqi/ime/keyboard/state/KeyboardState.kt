//KeyboardState.kt
package com.iqqi.ime.keyboard.state

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iqqi.ime.keyboard.model.KeyboardLanguage
import com.iqqi.ime.keyboard.model.KeyboardMode
import com.iqqi.ime.keyboard.model.KeyboardThemeSpec

val localKeyboardStyle = staticCompositionLocalOf<KeyboardThemeSpec> {
    error("No KeyboardStyle provided")
}

data class KeyboardState(
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val layoutConfig: LayoutConfig = LayoutConfig(),
    val deviceConfig: DeviceConfig = DeviceConfig()
)

data class LayoutConfig(
    val mode: KeyboardMode = KeyboardMode.LETTERS,
    val shiftState: ShiftState = ShiftState.OFF,
    val pageIndex: Int = 0,
    val showNumberRow: Boolean = false,
    val hasShift: Boolean = true,
)

data class DeviceConfig(
    val keyboardHeight: Dp = 400.dp,
    val isLandscape: Boolean = true,
)

enum class ShiftState {
    OFF,
    ON,        // 一次性
    CAPS_LOCK
}