package com.iqqi.dictionary

/**
 * ❌ 不要：
 * 	•	記住使用者狀態
 * 	•	學習
 * 	•	排序策略（那是 engine 的事）
 */
interface Dictionary {
    fun query(code: String): List<String>

    fun predict(previous: String): List<String>
}
