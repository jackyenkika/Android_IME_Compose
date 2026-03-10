package com.iqqi.keyboard

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.iqqi.core.ImeAction
import com.iqqi.data.SettingsRepository
import com.iqqi.ime.IMEService
import com.iqqi.ime.IMEStore
import com.iqqi.keyboard.controller.KeyboardController
import com.iqqi.keyboard.model.KeyType
import com.iqqi.keyboard.state.LayoutConfig
import com.iqqi.keyboard.ui.KeyboardLayout
import com.iqqi.keyboard.ui.KeyboardSizeCalculator
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(
    context: Context, private val repository: SettingsRepository
) : AbstractComposeView(context) {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val density = LocalDensity.current
        val config = LocalConfiguration.current

        val showDigital by repository.enableDigitalFlow.collectAsState(initial = repository.defaultSetting.enableDigital)
        val keyboardHeight by repository.keyboardHeightFlow.collectAsState(initial = repository.defaultSetting.keyboardHeight)
        val candidateHeight by repository.candidateHeightFlow.collectAsState(initial = repository.defaultSetting.candidateHeight)
        val themeColor by repository.themeColorFlow.collectAsState(initial = repository.defaultSetting.themeColor)
        val keyboardBackgroundImage by repository.keyboardBackgroundImageFlow.collectAsState(initial = repository.defaultSetting.backgroundImage)

        val ime = context as IMEService
        val controller = remember { KeyboardController(ime) }

        val candidateState by IMEStore.candidateState.collectAsState()
        val keyboardState by IMEStore.keyboardState.collectAsState()
        LaunchedEffect(showDigital) {
            val newState = keyboardState.copy(
                layoutConfig = LayoutConfig(
                    showNumberRow = showDigital
                )
            )
            IMEStore.updateKeyboardState(newState)
        }

        val deviceConfig = remember(
            keyboardHeight, candidateHeight, config.orientation
        ) {
            KeyboardSizeCalculator.getDeviceConfig(
                context = context,
                density = density,
                keyboardHeight = keyboardHeight,
                candidateHeight = candidateHeight,
                isLandscape = config.orientation == Configuration.ORIENTATION_LANDSCAPE
            )
        }

        val layout = remember(keyboardState.layoutConfig) {
            KeyboardLayoutProvider.create(keyboardState.layoutConfig)
        }
        KeyboardTheme(
            themeColor = themeColor, backgroundImage = keyboardBackgroundImage
        ) {
            KeyboardLayout(
                deviceConfig = deviceConfig,
                layout = layout,
                candidates = candidateState.candidates,
                onDeleteUp = { ime.onDeleteKeyUp() },
                onCandidateClick = { index ->
                    ime.dispatch(ImeAction.SelectCandidate(index))
                },
            ) { key ->
                if (key.type == KeyType.DELETE) {
                    ime.onDeleteKeyDown()
                } else {
                    val newState = controller.onKey(key, keyboardState)
                    IMEStore.updateKeyboardState(newState)
                }
            }
        }
    }
}