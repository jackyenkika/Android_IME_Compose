package com.iqqi.settings

import androidx.annotation.DrawableRes
import com.iqqi.ime.R

enum class KeyboardHeight(val scale: Float) {
    SMALL(0.25f),
    MEDIUM(0.3f),
    LARGE(0.4f),
    EXTRA_LARGE(0.5f)
}

enum class CandidateHeight(val scale: Float) {
    SMALL(0.7f),
    MEDIUM(0.8f),
    LARGE(0.9f),
}

enum class ThemeColor {
    BLUE,
    GREEN,
    PURPLE,
    RED,
    BLACK,
    WHITE
}

enum class BackgroundImage(
    val label: String, @DrawableRes val resId: Int?
) {
    NONE("None", null),
    FIFA_GREEN("FIFA Green", R.drawable.bg_fifa_green),
}