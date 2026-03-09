package com.iqqi.ime

import android.view.KeyEvent
import com.iqqi.core.ImeAction
import com.iqqi.core.Key
import com.iqqi.keyboard.model.KeySpec
import com.iqqi.keyboard.model.KeyType

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
class AndroidKeyMapper() {

    //軟體鍵盤
    fun map(key: KeySpec): ImeAction? {

        return when (key.type) {

            KeyType.INPUT -> {
                val label = key.label ?: return null
                ImeAction.Input(Key.Char(label.first()))
            }

            KeyType.DELETE -> {
                ImeAction.Delete
            }

            KeyType.SPACE -> {
                ImeAction.Input(Key.Space)
            }


            KeyType.ENTER -> {
                ImeAction.Input(Key.Enter)
            }

            else -> null
        }

    }

    //硬體鍵盤
    fun map(event: KeyEvent): ImeAction? {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DEL -> ImeAction.Delete
            else -> {
                event.unicodeChar
                    .takeIf { it != 0 }
                    ?.toChar()
                    ?.let { ImeAction.Input(Key.Char(it)) }
            }
        }
    }
}