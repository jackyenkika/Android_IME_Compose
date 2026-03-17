//KeyboardState.kt
package com.iqqi.keyboard.state

import com.iqqi.keyboard.model.ImeLanguage
import com.iqqi.keyboard.model.KeyboardMode

data class KeyboardState(
    val language: ImeLanguage = ImeLanguage(),
    val layoutConfig: LayoutConfig = LayoutConfig(),
    val inputType: Int = 0,
    val showLanguageMenu: Boolean = false,
    val animationTick: Int = 0,
)

data class LayoutConfig(
    val mode: KeyboardMode = KeyboardMode.LETTERS,
    val shiftState: ShiftState = ShiftState.OFF,
    val pageIndex: Int = 0,
    val showNumberRow: Boolean = false,
    val hasShift: Boolean = true,
)

enum class ShiftState {
    OFF,
    ON,        // 一次性
    CAPS_LOCK
}