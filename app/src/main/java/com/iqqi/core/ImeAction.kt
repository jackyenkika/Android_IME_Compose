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
    object Commit : ImeAction()
    object Reset : ImeAction()

    data class SwitchLayout(val layout: KeyboardType) : ImeAction()
    data class SelectCandidate(val index: Int) : ImeAction()
}