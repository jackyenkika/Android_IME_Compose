package com.iqqi.core

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
    object Space : Key()
    object Enter : Key()
}