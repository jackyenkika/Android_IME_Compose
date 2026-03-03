package com.iqqi.ime

import android.view.KeyEvent
import com.iqqi.core.ImeAction
import com.iqqi.core.Key
import com.iqqi.core.KeyboardType

/**
 * Android 世界 → Core 世界
 * KeyEvent → Key
 * KeyEvent → ImeAction
 *
 * ❌ 不要做
 * 	•	呼叫 engine
 * 	•	存 state
 * 	•	判斷拼音
 */
class AndroidKeyMapper(
    private val layoutProvider: () -> KeyboardType
) {

    fun map(event: KeyEvent): ImeAction? {
        return when (layoutProvider()) {
            KeyboardType.QWERTY -> mapQwerty(event)
            KeyboardType.T9 -> mapT9(event)
        }
    }

    private fun mapQwerty(event: KeyEvent): ImeAction? {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> ImeAction.Delete
            KeyEvent.KEYCODE_SPACE -> ImeAction.Input(Key.Space)
            else -> {
                event.unicodeChar
                    .takeIf { it != 0 }
                    ?.toChar()
                    ?.let { ImeAction.Input(Key.Char(it)) }
            }
        }
    }

    private fun mapT9(event: KeyEvent): ImeAction? {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> ImeAction.Delete
            KeyEvent.KEYCODE_SPACE -> ImeAction.Input(Key.Space)
            else -> {
                event.unicodeChar
                    .takeIf { it != 0 }
                    ?.toChar()
                    ?.let { ImeAction.Input(Key.Char(it)) }
            }
        }
    }
}