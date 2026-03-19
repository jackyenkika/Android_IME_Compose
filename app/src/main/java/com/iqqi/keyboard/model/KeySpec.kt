package com.iqqi.keyboard.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.iqqi.ime.BuildConfig
import com.iqqi.ime.util.ToolObj.expireTimestamp

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
    private val expireTimestamp: Long? = expireDateStr?.expireTimestamp()

    fun isExpired(now: Long = System.currentTimeMillis()): Boolean {
        if (expireTimestamp == null) return false
        return listOfNotNull(expireTimestamp, appExpireTimestamp).any { now > it }
    }

    companion object {
        private val appExpireTimestamp: Long? = BuildConfig.AppExpireDate.expireTimestamp()
    }
}