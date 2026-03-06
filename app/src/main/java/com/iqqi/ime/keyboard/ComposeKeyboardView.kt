package com.iqqi.ime.keyboard

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.controller.KeyboardController
import com.iqqi.ime.keyboard.state.DeviceConfig
import com.iqqi.ime.keyboard.state.KeyboardState
import com.iqqi.ime.keyboard.state.LayoutConfig
import com.iqqi.ime.keyboard.ui.KeyboardLayout
import com.iqqi.ime.keyboard.ui.KeyboardSizeCalculator
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(context: Context) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val density = LocalDensity.current
        val config = LocalConfiguration.current

        val repository = remember { SettingsRepository(context) }

        val showDigital by repository.enableDigitalFlow.collectAsState(initial = repository.defaultSetting.enableDigital)
        val keyboardHeight by repository.keyboardHeightFlow.collectAsState(initial = repository.defaultSetting.keyboardHeight)
        val themeColor by repository.themeColorFlow.collectAsState(initial = repository.defaultSetting.themeColor)
        val keyboardBackgroundImage by repository.keyboardBackgroundImageFlow.collectAsState(initial = repository.defaultSetting.backgroundImage)

        var keyboardState by remember(
            showDigital,
            keyboardHeight,
            config.orientation
        ) {
            mutableStateOf(
                KeyboardState(
                    layoutConfig = LayoutConfig(
                        showNumberRow = showDigital
                    ),
                    deviceConfig = DeviceConfig(
                        keyboardHeight = KeyboardSizeCalculator.getKeyboardTotalHeight(
                            context,
                            density,
                            keyboardHeight,
                            config.orientation == Configuration.ORIENTATION_LANDSCAPE
                        ),
                        isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
                    )
                )
            )
        }

        val ime = context as IMEService

        val controller = remember { KeyboardController(ime) }

        val layout = remember(keyboardState.layoutConfig) {
            KeyboardLayoutProvider.create(keyboardState.layoutConfig)
        }
        KeyboardTheme(
            themeColor = themeColor,
            backgroundImage = keyboardBackgroundImage
        ) {
            KeyboardLayout(
                keyboardHeightDp = keyboardState.deviceConfig.keyboardHeight,
                layout = layout,
                candidates = emptyList(),
                onCandidateClick = { },
            ) { key ->
                keyboardState = controller.onKey(key, keyboardState)
            }
        }
    }
}