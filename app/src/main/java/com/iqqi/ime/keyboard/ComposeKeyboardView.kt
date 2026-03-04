package com.iqqi.ime.keyboard

import android.content.Context
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.ui.KeyboardLayout
import com.iqqi.settings.KeyboardHeight
import com.iqqi.settings.ThemeColor
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(context: Context) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val repository = remember { SettingsRepository(context) }

        val themeColor by repository.themeColorFlow.collectAsState(initial = ThemeColor.BLUE)
        val keyboardHeightScale by repository.keyboardHeightFlow.collectAsState(initial = KeyboardHeight.MEDIUM.scale)

        KeyboardTheme(themeColor = themeColor) {
            KeyboardLayout(scale = keyboardHeightScale) { key ->

                val ic = (context as IMEService).currentInputConnection

                when (key.type) {

                    KeyType.INPUT -> {
                        ic.commitText(key.label ?: "", 1)
                    }

                    KeyType.SHIFT -> {

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