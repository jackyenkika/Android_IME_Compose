package com.iqqi.keyboard.state

data class CandidateState(
    val candidates: List<String> = emptyList(),
    val indexMap: List<Int> = emptyList() // 關鍵：記錄 UI 索引對應原始索引
)