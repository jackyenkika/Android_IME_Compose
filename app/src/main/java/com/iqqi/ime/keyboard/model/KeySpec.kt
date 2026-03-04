package com.iqqi.ime.keyboard.model

import androidx.compose.ui.graphics.vector.ImageVector

data class KeySpec(
    val label: String? = null,
    val code: Int? = null,
    val icon: ImageVector? = null,
    val type: KeyType = KeyType.INPUT,
    val weight: Float = 1f,
    val isRepeatable: Boolean = false
)