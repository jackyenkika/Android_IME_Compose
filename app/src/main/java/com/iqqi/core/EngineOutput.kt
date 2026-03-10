package com.iqqi.core

/**
 * UI 唯一資料來源
 *
 * ❗️注意：
 * 	•	commitText 是「一次性事件」
 * 	•	candidates / composing 是「狀態投影」
 */
data class EngineOutput(
    val composingText: String?,
    val candidates: List<String> = emptyList(),
    val selectedIndex: Int = 0,
    val mode: InputMode,
    val commitText: String? = null,
    val deleteBeforeCursor: Boolean,
)