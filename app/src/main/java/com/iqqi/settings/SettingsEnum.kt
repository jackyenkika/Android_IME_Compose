package com.iqqi.settings

import androidx.annotation.DrawableRes
import com.iqqi.ime.BuildConfig
import com.iqqi.ime.R
import java.text.SimpleDateFormat
import java.util.Locale

enum class KeyboardHeight(val horizontalScale: Float, val verticalScale: Float) {
    SMALL(0.55f, 0.25f),
    MEDIUM(0.6f, 0.3f),
    LARGE(0.65f, 0.35f),
    EXTRA(0.7f, 0.4f)
}

enum class CandidateHeight(val scale: Float) {
    SMALL(0.4f),
    MEDIUM(0.45f),
    LARGE(0.5f),
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
    val label: String, @DrawableRes val resId: Int?, val expireDateStr: String? = null
) {
    NONE("None", null, null),
    FIFA_GREEN(
        "FIFA Green",
        R.drawable.bg_fifa2026_country_colours_gn,
        BuildConfig.Fifa2026ExpireDate
    ),
    FIFA_BLUE(
        "FIFA Blue",
        R.drawable.bg_fifa2026_country_colours_bl,
        BuildConfig.Fifa2026ExpireDate
    ),
    FIFA_RED("FIFA Red", R.drawable.bg_fifa2026_country_colours_rd, BuildConfig.Fifa2026ExpireDate),
    FIFA_TOURNAMENT(
        "FIFA Tournament",
        R.drawable.bg_fifa2026_tournament_colours,
        BuildConfig.Fifa2026ExpireDate
    ),
    FIFA_ACCESSIBILITY(
        "FIFA Accessibility",
        R.drawable.bg_fifa2026_accessibility_colours,
        BuildConfig.Fifa2026ExpireDate
    ),
    ;

    private val expireTimestamp: Long? = expireDateStr?.let {
        try {
            SimpleDateFormat("yyyyMMddHHmmss", Locale.US).parse(it)?.time
        } catch (e: Exception) {
            null
        }
    }

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean =
        expireTimestamp?.let { now > it } ?: false

    fun isAvailable(): Boolean = !isExpired()

    companion object {
        fun availableEntries(): List<BackgroundImage> = entries.filter { it.isAvailable() }
        fun fallback(): BackgroundImage = NONE
    }
}