package com.iqqi.ime.keyboard.model

data class KeySpec(
    val label: String? = null,
    val code: Int? = null,
    val type: KeyType = KeyType.INPUT,
    val weight: Float = 1f,
    val isRepeatable: Boolean = false
)