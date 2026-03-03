package com.iqqi.ime.keyboard

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalContext
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.keyboard.ui.KeyboardLayout
import com.iqqi.settings.ThemeColor
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(context: Context) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val repository = remember { SettingsRepository(context) }

        val themeColor by repository.themeColorFlow.collectAsState(initial = ThemeColor.BLUE)
        val keyboardHeightScale by repository.keyboardHeightFlow.collectAsState(initial = 0.4f)

        KeyboardTheme(themeColor = themeColor) {
            KeyboardLayout(scale = keyboardHeightScale)
        }
    }
}