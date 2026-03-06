package com.iqqi.ime.keyboard.ui

import android.content.Context
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.iqqi.settings.KeyboardHeight

object KeyboardSizeCalculator {
    fun getKeyboardTotalHeight(
        context: Context,
        density: Density,
        keyboardHeight: KeyboardHeight,
        isLandscape: Boolean
    ): Dp {
        val scale =
            if (isLandscape) keyboardHeight.horizontalScale else keyboardHeight.verticalScale
        // 獲取目前的螢幕密度與高度
        val screenHeightPx = context.resources.displayMetrics.heightPixels
        val keyboardHeightDp = with(density) { (screenHeightPx * scale).toDp() }
        return keyboardHeightDp
    }

}