package com.iqqi.ime.keyboard

import android.content.Context
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.model.KeyboardMode
import com.iqqi.ime.keyboard.state.KeyboardState
import com.iqqi.ime.keyboard.state.ShiftState
import com.iqqi.ime.keyboard.ui.KeyboardLayout
import com.iqqi.settings.BackgroundImage
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.ThemeColor
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(context: Context) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val repository = remember { SettingsRepository(context) }

        val showDigital by repository.enableDigitalFlow.collectAsState(initial = true)
        val themeColor by repository.themeColorFlow.collectAsState(initial = ThemeColor.BLUE)
        val keyboardBackgroundImage by repository.keyboardBackgroundImageFlow.collectAsState(initial = BackgroundImage.NONE)

        val keyboardHeightScale by repository.keyboardHeightFlow.collectAsState(initial = KeyboardHeight.MEDIUM.scale)
        var lastShiftClickTime by remember { mutableStateOf(0L) }

        var state by remember { mutableStateOf(KeyboardState()) }
        state = state.copy(
            layoutConfig = state.layoutConfig.copy(
                showNumberRow = showDigital
            )
        )
        val layout = KeyboardLayoutProvider.create(state.layoutConfig)
        KeyboardTheme(
            themeColor = themeColor,
            backgroundImage = keyboardBackgroundImage
        ) {
            KeyboardLayout(scale = keyboardHeightScale, layout = layout) { key ->

                val ic = (context as IMEService).currentInputConnection

                when (key.type) {

                    KeyType.INPUT -> {
                        ic.commitText(key.label ?: "", 1)

                        if (state.layoutConfig.shiftState == ShiftState.ON) {
                            state = state.copy(
                                layoutConfig = state.layoutConfig.copy(
                                    shiftState = ShiftState.OFF
                                )
                            )
                        }
                    }

                    KeyType.SHIFT -> {
                        val now = System.currentTimeMillis()

                        val newShiftState = when {

                            // 雙擊 → CAPS_LOCK
                            now - lastShiftClickTime < 400 -> {
                                if (state.layoutConfig.shiftState == ShiftState.CAPS_LOCK)
                                    ShiftState.OFF
                                else
                                    ShiftState.CAPS_LOCK
                            }

                            // 單擊切換
                            else -> {
                                when (state.layoutConfig.shiftState) {
                                    ShiftState.OFF -> ShiftState.ON
                                    ShiftState.ON -> ShiftState.OFF
                                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                                }
                            }
                        }

                        lastShiftClickTime = now

                        state = state.copy(
                            layoutConfig = state.layoutConfig.copy(
                                shiftState = newShiftState
                            )
                        )
                    }

                    KeyType.DELETE -> {
                        ic.deleteSurroundingText(1, 0)
                    }

                    KeyType.SPACE -> {
                        ic.commitText(" ", 1)
                    }

                    KeyType.ENTER -> {
                        ic.sendKeyEvent(
                            KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                        )
                    }

                    KeyType.SYMBOL -> {
                        val newMode =
                            if (state.layoutConfig.mode == KeyboardMode.LETTERS)
                                KeyboardMode.SYMBOLS
                            else
                                KeyboardMode.LETTERS

                        state = state.copy(
                            layoutConfig = state.layoutConfig.copy(
                                mode = newMode,
                                pageIndex = 0,
                                shiftState = ShiftState.OFF
                            )
                        )
                    }

                    KeyType.NEXT_SYMBOL -> {
                        val totalPages = KeyboardLayoutProvider.symbolPageCount

                        state = state.copy(
                            layoutConfig = state.layoutConfig.copy(
                                pageIndex = (state.layoutConfig.pageIndex + 1) % totalPages
                            )
                        )
                    }

                    KeyType.LANGUAGE -> {
                        context.switchToNextInputMethod(false)
                    }

                    KeyType.SETTINGS -> {
                        val intent = android.content.Intent(
                            context, com.iqqi.settings.SettingsActivity::class.java
                        )
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }

                    KeyType.CANCEL -> {
                        context.requestHideSelf(0)
                    }
                }
            }
        }
    }
}