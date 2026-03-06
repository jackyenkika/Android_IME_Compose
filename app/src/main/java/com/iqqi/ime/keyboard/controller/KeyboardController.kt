package com.iqqi.ime.keyboard.controller

import android.content.Intent
import android.view.KeyEvent
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.KeyboardLayoutProvider
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.model.KeyboardMode
import com.iqqi.ime.keyboard.state.KeyboardState
import com.iqqi.ime.keyboard.state.ShiftState
import com.iqqi.settings.SettingsActivity

class KeyboardController(
    private val ime: IMEService
) {

    private val shiftController = ShiftController()

    fun onKey(
        key: KeySpec,
        state: KeyboardState
    ): KeyboardState {

        val ic = ime.currentInputConnection

        return when (key.type) {

            KeyType.INPUT -> {
                ic.commitText(key.label ?: "", 1)

                if (state.layoutConfig.shiftState == ShiftState.ON) {
                    state.copy(
                        layoutConfig = state.layoutConfig.copy(
                            shiftState = ShiftState.OFF
                        )
                    )
                } else {
                    state
                }
            }

            KeyType.DELETE -> {
                ic.deleteSurroundingText(1, 0)
                state
            }

            KeyType.SPACE -> {
                ic.commitText(" ", 1)
                state
            }

            KeyType.ENTER -> {
                ic.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
                state
            }

            KeyType.SHIFT -> {
                val newShift = shiftController.nextState(
                    state.layoutConfig.shiftState
                )

                state.copy(
                    layoutConfig = state.layoutConfig.copy(
                        shiftState = newShift
                    )
                )
            }

            KeyType.SYMBOL -> {

                val newMode =
                    if (state.layoutConfig.mode == KeyboardMode.LETTERS)
                        KeyboardMode.SYMBOLS
                    else
                        KeyboardMode.LETTERS

                state.copy(
                    layoutConfig = state.layoutConfig.copy(
                        mode = newMode,
                        pageIndex = 0,
                        shiftState = ShiftState.OFF
                    )
                )
            }

            KeyType.NEXT_SYMBOL -> {

                val totalPages = KeyboardLayoutProvider.symbolPageCount

                state.copy(
                    layoutConfig = state.layoutConfig.copy(
                        pageIndex =
                            (state.layoutConfig.pageIndex + 1) % totalPages
                    )
                )
            }

            KeyType.LANGUAGE -> {
                ime.switchToNextInputMethod(false)
                state
            }

            KeyType.SETTINGS -> {
                val intent = Intent(
                    ime,
                    SettingsActivity::class.java
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ime.startActivity(intent)
                state
            }

            KeyType.CANCEL -> {
                ime.requestHideSelf(0)
                state
            }
        }
    }
}