package com.iqqi.settings.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.iqqi.keyboard.model.KeyboardThemeSpec
import com.iqqi.keyboard.ui.localKeyboardStyle
import com.iqqi.settings.BackgroundImage
import com.iqqi.settings.ThemeColor

@Composable
fun KeyboardTheme(
    themeColor: ThemeColor,
    backgroundImage: BackgroundImage? = null,
    content: @Composable () -> Unit
) {
    //2026.03.06 暫時固定暗夜配色
//    val isDark = true
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()

    val colorScheme = when (themeColor) {

        ThemeColor.BLUE -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFF82B1FF),
                    background = Color(0xFF0D1117),
                    surface = Color(0xFF161B22)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF2962FF),
                    background = Color(0xFFF5F8FF),
                    surface = Color.White
                )
            }
        }

        ThemeColor.GREEN -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFF66BB6A),
                    background = Color(0xFF0F1A12),
                    surface = Color(0xFF162117)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF2E7D32),
                    background = Color(0xFFF3FBF4),
                    surface = Color.White
                )
            }
        }

        ThemeColor.PURPLE -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFCE93D8),
                    background = Color(0xFF181022),
                    surface = Color(0xFF21162C)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF6A1B9A),
                    background = Color(0xFFF8F2FB),
                    surface = Color.White
                )
            }
        }

        ThemeColor.RED -> {
            if (isDark) {
                darkColorScheme(
                    primary = Color(0xFFEF5350),
                    background = Color(0xFF1B0F0F),
                    surface = Color(0xFF261616)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFC62828),
                    background = Color(0xFFFFF5F5),
                    surface = Color.White
                )
            }
        }

        ThemeColor.BLACK -> {
            darkColorScheme(
                primary = Color.White,
                background = Color.Black,
                surface = Color(0xFF121212)
            )
        }

        ThemeColor.WHITE -> {
            lightColorScheme(
                primary = Color.Black,
                background = Color.White,
                surface = Color(0xFFF2F2F2)
            )
        }
    }

    val painter = backgroundImage?.toPainterOrNull()

    val keyboardStyle = KeyboardThemeSpec(
        backgroundColor =
            if (painter == null) colorScheme.background else colorScheme.background.copy(alpha = 0.0f),
        backgroundImage = painter,

        candidateBackgroundColor = if (painter == null) colorScheme.surfaceVariant
        else colorScheme.surfaceVariant.copy(alpha = 0.2f),
        candidateTextColor = colorScheme.onSurfaceVariant,
        candidateTextBackgroundColor = listOf(Color.Transparent, Color.Transparent),

        candidateSpecialTextColor = Color(0xFF3A2F00),
        candidateSpecialTextBackgroundColor = listOf(
            Color(0xFFFFF6B0), // 高光
            Color(0xFFFFD700), // 金色
            Color(0xFFE6B800), // 暗金
            Color(0xFFFFD700), // 回亮
        ),

        keyBackgroundColor =
            if (painter == null) colorScheme.surface else colorScheme.surface.copy(alpha = 0.2f),
        keyTextColor = colorScheme.onSurface,

        keyFunctionalBackgroundColor =
            if (painter == null) colorScheme.surfaceVariant else colorScheme.surfaceVariant.copy(
                alpha = 0.5f
            ),
        keyFunctionalTextColor = colorScheme.onSurface,

        keyPrimaryBackgroundColor =
            if (painter == null) colorScheme.primary else colorScheme.primary.copy(alpha = 0.5f),
        keyPrimaryTextColor = colorScheme.onPrimary,

        keyPreviewedColor = colorScheme.primary.copy(alpha = 0.95f),
        keyPreviewTextColor = colorScheme.onPrimary,

        keyPressedColor = colorScheme.primary.copy(alpha = 0.2f),

        keyBorderColor = colorScheme.primary.copy(alpha = 0.3f),
    )

    MaterialTheme(
        colorScheme = colorScheme,
    ) {
        CompositionLocalProvider(
            localKeyboardStyle provides keyboardStyle
        ) {
            content()
        }
    }
}

@Composable
fun BackgroundImage.toPainterOrNull(): Painter? {
    return resId?.let {
        painterResource(it)
    }
}