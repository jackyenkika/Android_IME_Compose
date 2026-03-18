package com.iqqi.keyboard.controller

import android.content.Intent
import com.iqqi.ime.IMEKeyMapper
import com.iqqi.ime.IMEService
import com.iqqi.ime.IMEStore
import com.iqqi.ime.util.LogObj
import com.iqqi.keyboard.KeyboardLayoutProvider
import com.iqqi.keyboard.model.KeySpec
import com.iqqi.keyboard.model.KeyType
import com.iqqi.keyboard.model.KeyboardMode
import com.iqqi.keyboard.state.KeyboardState
import com.iqqi.keyboard.state.ShiftState
import com.iqqi.settings.SettingsActivity

class KeyboardController(
    private val ime: IMEService
) {

    private val mapper = IMEKeyMapper()
    private val shiftController = ShiftController()

    fun onKey(key: KeySpec, state: KeyboardState): KeyboardState {

        // 1️⃣ 軟鍵盤 / 功能鍵映射成 ImeAction
        mapper.map(key)?.let {
            ime.dispatch(it)

            if (key.type == KeyType.ENTER) {
                if (key.iconDrawable != null && !key.iconDrawable.isExpired()) {
                    LogObj.trace("Enter key with animation triggered")
                    return state.copy(
                        animationShakeTick = state.animationShakeTick + 1
                    )
                }
            }

            // 如果是輸入鍵，shift ON 則自動切回 OFF
            if (key.type == KeyType.INPUT && state.layoutConfig.shiftState == ShiftState.ON) {
                return state.copy(
                    layoutConfig = state.layoutConfig.copy(
                        shiftState = ShiftState.OFF
                    )
                )
            }

            return state
        }

        return when (key.type) {
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
                state.copy(
                    showLanguageMenu = !state.showLanguageMenu
                )
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

            KeyType.STICKER -> {
                if (ime.canCommitSticker()) {
                    IMEStore.updateStickerState(true)
                }
                state
            }

            KeyType.BACK -> {
                IMEStore.updateStickerState(false)
                state
            }

            else -> state
        }
    }
}