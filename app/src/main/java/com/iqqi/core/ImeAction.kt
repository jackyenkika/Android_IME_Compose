package com.iqqi.core

/**
 * 只描述「發生了什麼」，不描述「怎麼做」
 *
 * ❌ 不要放：
 * 	•	拼音
 * 	•	language
 * 	•	qwerty / t9
 * 	•	keyCode = Int
 */
sealed class ImeAction {

    data class Input(val key: Key) : ImeAction()
    object Delete : ImeAction()
    object Reset : ImeAction()
    data class SelectCandidate(val index: Int) : ImeAction()
}

/**
 * 要「小」，而且不可逆
 *
 * ❌ 不要出現：
 * 	•	Android KeyEvent
 * 	•	Int keyCode
 * 	•	Shift / Ctrl（這是 UI 的事）
 */
sealed class Key {
    data class Char(val c: kotlin.Char) : Key()
    data class Text(val text: String) : Key()
    object Space : Key()
    object Enter : Key()
}