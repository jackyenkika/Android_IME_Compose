package com.iqqi.keyboard.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Locale

data class KeySpec(
    val label: String? = null,
    val code: Int? = null,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: IconImage? = null,
    val type: KeyType = KeyType.INPUT,
    val weight: Float = 1f,
    val isRepeatable: Boolean = false,
    val altChars: List<String> = emptyList(),
    val isEnable: Boolean = true,
)

data class IconImage(
    val label: String, @DrawableRes val resId: Int?, val expireDateStr: String? = null
) {
    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        val expire = expireDateStr ?: return false

        return try {
            val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            val date = sdf.parse(expire) ?: return false
            now > date.time
        } catch (e: Exception) {
            false
        }
    }
}