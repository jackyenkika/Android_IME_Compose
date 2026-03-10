package com.iqqi.keyboard.state

data class CandidateState(
    val candidates: List<String> = emptyList(),
    val selectedIndex: Int = 0
)