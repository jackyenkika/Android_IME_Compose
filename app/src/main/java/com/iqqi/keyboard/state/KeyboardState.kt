//KeyboardState.kt
package com.iqqi.keyboard.state

import com.iqqi.keyboard.model.KeyboardLanguage
import com.iqqi.keyboard.model.KeyboardMode

data class KeyboardState(
    val language: KeyboardLanguage = KeyboardLanguage.ENGLISH,
    val layoutConfig: LayoutConfig = LayoutConfig(),
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