package com.iqqi.settings

import androidx.annotation.DrawableRes
import com.iqqi.ime.R

enum class KeyboardHeight(val horizontalScale: Float, val verticalScale: Float) {
    SMALL(0.55f, 0.25f),
    MEDIUM(0.6f, 0.3f),
    LARGE(0.65f, 0.35f),
    EXTRA(0.7f, 0.4f)
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