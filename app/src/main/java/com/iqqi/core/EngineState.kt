package com.iqqi.core

/**
 * 所有狀態都可見
 * 你之後會一直往這裡加東西，但請忍住
 *
 * @param  buffer：使用者打的 raw key。ex:"nihao"
 * @param composing：語言模型算出的顯示字。ex:"你好"
 */
data class EngineState(
    val buffer: String = "",
    val composing: String = "",
    val candidates: List<String> = emptyList(),
    val layout: KeyboardType = KeyboardType.QWERTY,
    var mode: InputMode = InputMode.Idle,
    val selectedIndex: Int = 0, // 選字索引，可在 Selecting mode 使用
    val commitText: String? = null,
    var predictingCandidates: List<String> = emptyList(), // 二元聯想候選
)

/**
 * Engine 的「行為狀態」
 *
 * 它決定：
 * 	•	Delete 是刪 buffer 還是取消候選
 * 	•	Space 是 commit 還是插入空白
 * 	•	Key input 是累積還是忽略
 */
enum class InputMode {
    Idle,          // 沒有輸入
    Composing,     // 正在輸入拼音
    Selecting,      // 正在選候選
    Predicting,     // 二元聯想候選中
}