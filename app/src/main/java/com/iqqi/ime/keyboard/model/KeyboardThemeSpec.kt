package com.iqqi.ime.keyboard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyboardThemeSpec(
    val backgroundColor: Color,
    val keyBackgroundColor: Color,
    val keyPressedColor: Color,
    val keyBorderColor: Color,
    val keyTextColor: Color,
    val keyPreviewedColor: Color,
    val keyPreviewTextColor: Color,
    val keyCornerRadius: Dp = 8.dp,
    val backgroundImage: Painter? = null,
)