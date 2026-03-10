package com.iqqi.keyboard.ui

import android.content.Context
import androidx.compose.ui.unit.Density
import com.iqqi.settings.CandidateHeight
import com.iqqi.settings.KeyboardHeight

object KeyboardSizeCalculator {

    fun getDeviceConfig(
        context: Context,
        density: Density,
        keyboardHeight: KeyboardHeight,
        candidateHeight: CandidateHeight,
        isLandscape: Boolean
    ): DeviceConfig {
        val keyboardScale =
            if (isLandscape) keyboardHeight.horizontalScale else keyboardHeight.verticalScale
        val candidateScale = if (isLandscape) 0.09f else 0.05f
        // 獲取目前的螢幕密度與高度
        val screenHeightPx = context.resources.displayMetrics.heightPixels
        val keyboardHeightDp = with(density) { (screenHeightPx * keyboardScale).toDp() }
        val candidateHeightDp = with(density) { (screenHeightPx * candidateScale).toDp() }
        return DeviceConfig(keyboardHeightDp, candidateHeightDp, candidateHeight.scale)
    }
}