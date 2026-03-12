package com.iqqi.keyboard.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class KeyboardThemeSpec(
    val backgroundColor: Color,
    val backgroundImage: Painter? = null,

    //候選字區顏色
    val candidateBackgroundColor: Color,
    val candidateTextColor: Color,

    //普通按鍵
    val keyBackgroundColor: Color,
    val keyTextColor: Color,
    //功能型按鍵 shift / delete / settings
    val keyFunctionalBackgroundColor: Color,
    val keyFunctionalTextColor: Color,
    //enter / send / search
    val keyPrimaryBackgroundColor: Color,
    val keyPrimaryTextColor: Color,

    val keyPressedColor: Color,

    val keyBorderColor: Color,

    val keyPreviewedColor: Color,
    val keyPreviewTextColor: Color,

    val keyCornerRadius: Dp = 8.dp,
)