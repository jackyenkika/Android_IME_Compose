package com.iqqi.keyboard

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.iqqi.core.ImeAction
import com.iqqi.data.SettingsRepository
import com.iqqi.data.StickerRepository
import com.iqqi.ime.IMEService
import com.iqqi.ime.IMEStore
import com.iqqi.keyboard.controller.KeyboardController
import com.iqqi.keyboard.model.KeySpec
import com.iqqi.keyboard.model.KeyType
import com.iqqi.keyboard.state.LayoutConfig
import com.iqqi.keyboard.ui.AnimationConfig
import com.iqqi.keyboard.ui.KeyboardLayout
import com.iqqi.keyboard.ui.KeyboardSizeCalculator
import com.iqqi.keyboard.ui.LanguageMenu
import com.iqqi.keyboard.ui.OverlayConfig
import com.iqqi.keyboard.ui.StickerPanel
import com.iqqi.settings.ui.KeyboardTheme

class ComposeKeyboardView(
    context: Context,
    private val repository: SettingsRepository,
    private val stickerRepository: StickerRepository
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
        val candidateState by IMEStore.candidateState.collectAsState()
        val keyboardState by IMEStore.keyboardState.collectAsState()
        val stickerState by IMEStore.stickerState.collectAsState()

        val controller = remember { KeyboardController(ime) }

        val canUseSticker = ime.canCommitSticker()

        val specialCandidates = remember {
            setOf("goal", "football", "worldcup")
        }

        // 1️⃣ 取得所有語言
        val allLanguages = remember { IMEService.getAvailableLanguages(context) }

        // 2️⃣ 取得 DataStore 儲存的已啟用語言 locale
        val enabledLocales by repository.enabledLanguagesFlow.collectAsState(initial = emptySet())

        // 3️⃣ 將所有語言標記 enabled 狀態
        val languages = allLanguages.map { lang ->
            lang.copy(enabled = enabledLocales.contains(lang.locale))
        }
        val currentLanguage = keyboardState.language

        LaunchedEffect(showDigital) {
            val newState = keyboardState.copy(
                layoutConfig = LayoutConfig(
                    showNumberRow = showDigital
                )
            )
            IMEStore.updateKeyboardState(newState)
        }

        LaunchedEffect(Unit) {

            IMEStore.commitEvents.collect { word ->
//                LogObj.trace("commit event = $word")
                if (specialCandidates.contains(word.lowercase())) {

                    val newState = keyboardState.copy(animationTick = true)
                    IMEStore.updateKeyboardState(newState)
                }
            }
        }

        // 專門給 shake / enter overlay 的 state
        val shakeOffset = remember { androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(keyboardState.animationShakeTick) {
            if (keyboardState.animationShakeTick == 0) return@LaunchedEffect
            // Shake 動畫
            val shakeAnim = listOf(0f, -10f, 10f, -5f, 5f, 0f)
            for (v in shakeAnim) {
                shakeOffset.animateTo(v, tween(40))
            }
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

        val layout = remember(keyboardState.layoutConfig, currentLanguage.name) {
            KeyboardLayoutProvider.create(keyboardState.layoutConfig, currentLanguage.name)
        }
        KeyboardTheme(
            themeColor = themeColor, backgroundImage = keyboardBackgroundImage
        ) {
            KeyboardLayout(
                deviceConfig = deviceConfig,
                layout = layout,
                candidates = candidateState.candidates,
                specialCandidates = specialCandidates,
                candidateFunctions = if (stickerState.visible) {
                    listOf(
                        KeySpec(type = KeyType.BACK, icon = Icons.Default.ArrowBackIosNew)
                    )
                } else {
                    listOf(
                        KeySpec(type = KeyType.SETTINGS, icon = Icons.Default.Settings),
                        KeySpec(type = KeyType.LANGUAGE, icon = Icons.Default.Language),
                        KeySpec(
                            type = KeyType.STICKER,
                            icon = Icons.Default.Gif,
                            isEnable = canUseSticker
                        )
                    )
                },
                animationConfig = AnimationConfig(
                    shakeOffset = shakeOffset.value, showAnimation = keyboardState.animationTick
                ),
                overlay = when {
                    stickerState.visible -> {
                        {
                            StickerPanel(
                                packs = stickerRepository.getStickerPacks(),
                                onStickerClick = { sticker ->
                                    ime.commitSticker(sticker)
                                    stickerRepository.addRecent(sticker)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    keyboardState.showLanguageMenu -> {
                        {
                            LanguageMenu(
                                languages = languages.filter { it.enabled },
                                current = currentLanguage,
                                onSelect = { lang ->
                                    ime.switchLanguage(lang)
                                },
                                onDismiss = {
                                    val newState = keyboardState.copy(showLanguageMenu = false)
                                    IMEStore.updateKeyboardState(newState)
                                })
                        }
                    }

                    else -> null
                },
                overlayConfig = OverlayConfig(
                    alpha = when {
                        stickerState.visible -> 0.95f
                        keyboardState.showLanguageMenu -> 0.2f
                        else -> 0f
                    }
                ),
                onDeleteUp = { ime.onDeleteKeyUp() },
                onAnimationEnd = {
                    if (keyboardState.animationTick) {
                        val newState = keyboardState.copy(animationTick = false)
                        IMEStore.updateKeyboardState(newState)
                    }
                },
                onCandidateClick = { index ->
                    ime.dispatch(ImeAction.SelectCandidate(index))
                },
                onKeyCommit = { key ->
                    when (key.type) {
                        KeyType.DELETE -> ime.onDeleteKeyDown()
                        else -> {
                            val newState = controller.onKey(key, keyboardState)
                            IMEStore.updateKeyboardState(newState)
                        }
                    }
                })
        }
    }
}