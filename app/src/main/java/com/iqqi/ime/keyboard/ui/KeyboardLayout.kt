package com.iqqi.ime.keyboard.ui


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.layout.englishLayout
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.model.KeyboardLanguage
import com.iqqi.ime.keyboard.state.KeyboardState
import com.iqqi.ime.keyboard.state.localKeyboardStyle

@Composable
fun KeyboardLayout(scale: Float) {
    var state by remember {
        mutableStateOf(KeyboardState())
    }

    val context = LocalContext.current
    val density = LocalDensity.current // 獲取目前的螢幕密度

    val layout = when (state.language) {
        KeyboardLanguage.ENGLISH -> englishLayout
//        KeyboardLanguage.CHINESE -> chineseLayout
//        KeyboardLanguage.JAPANESE -> japaneseLayout
        else -> englishLayout
    }
    val screenHeightPx = context.resources.displayMetrics.heightPixels
//    val keyboardHeight = (screenHeight * scale).dp
    val keyboardHeightDp = with(density) {
        (screenHeightPx * scale).toDp()
    }
    val rowHeight = keyboardHeightDp / layout.size

    Log.d(
        "zxc",
        "screenHeightPx: $screenHeightPx , scale = $scale , keyboardHeightDp = $keyboardHeightDp , rowHeight = $rowHeight"
    )
    val style = localKeyboardStyle.current
    Column(
        modifier = Modifier
            .background(style.backgroundColor)
            .height(keyboardHeightDp)
            .fillMaxWidth()
    ) {
        layout.forEach { row ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
            ) {
                Row(Modifier.fillMaxSize()) {
                    row.forEach { key ->
                        KeyboardKey(keyboardKey = key, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun KeyboardKey(
    keyboardKey: KeySpec, modifier: Modifier
) {
    val style = localKeyboardStyle.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState()
    val ctx = LocalContext.current
    Box(
        modifier = modifier
            .padding(2.dp)
            .background(
                if (pressed.value) style.keyPressedColor else style.keyBackgroundColor,
                RoundedCornerShape(style.keyCornerRadius)
            )
            .border(1.dp, style.keyBorderColor, RoundedCornerShape(style.keyCornerRadius))
            .fillMaxHeight(), contentAlignment = Alignment.BottomCenter
    ) {
        when (keyboardKey.type) {
            KeyType.INPUT -> {
                val label = keyboardKey.label ?: ""

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = interactionSource, indication = null) {
                            (ctx as IMEService).currentInputConnection
                                .commitText(label, label.length)
                        }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {

                    var textSize by remember { mutableStateOf(24.sp) }

                    Text(
                        text = label,
                        color = style.keyTextColor,
                        maxLines = 1,
                        fontSize = textSize,
                        onTextLayout = { result ->

                            if (result.didOverflowWidth || result.didOverflowHeight) {
                                textSize *= 0.9f
                            }
                        }
                    )
                }
            }

            KeyType.SHIFT -> KeyIcon(
                icon = Icons.Default.KeyboardArrowUp, onClick = {
                    // 之後改成改 state
                })

            KeyType.DELETE -> {
                KeyIcon(
                    icon = Icons.Default.Backspace, onClick = {
                        (ctx as IMEService).currentInputConnection.deleteSurroundingText(1, 0)
                    })
            }

            KeyType.SPACE -> {
                KeyIcon(
                    icon = Icons.Default.SpaceBar, onClick = {
                        (ctx as IMEService).currentInputConnection.commitText(" ", 1)
                    })
            }

            KeyType.ENTER -> {
                KeyIcon(
                    icon = Icons.Default.KeyboardReturn, onClick = {
                        (ctx as IMEService).currentInputConnection.sendKeyEvent(
                            android.view.KeyEvent(
                                android.view.KeyEvent.ACTION_DOWN,
                                android.view.KeyEvent.KEYCODE_ENTER
                            )
                        )
                    })
            }

            KeyType.SYMBOL -> {
                KeyIcon(
                    icon = Icons.Default.DashboardCustomize, onClick = {
                        (ctx as IMEService).requestHideSelf(0)
                    })
            }

            KeyType.LANGUAGE -> {
                KeyIcon(
                    icon = Icons.Default.Language, onClick = {
                        // 如果你要切換整個 IME
                        (ctx as IMEService).switchToNextInputMethod(false)

                        // 如果是自己內部切語言
                        // 改 state
                    })
            }

            KeyType.SETTINGS -> {
                KeyIcon(
                    icon = Icons.Default.Settings, onClick = {
                        val intent = android.content.Intent(
                            ctx, com.iqqi.settings.SettingsActivity::class.java
                        )
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        ctx.startActivity(intent)
                    })
            }

            KeyType.CANCEL -> {
                KeyIcon(
                    icon = Icons.Default.ArrowDropDown, onClick = {
                        (ctx as IMEService).requestHideSelf(0)
                    })
            }
        }

    }
}

@Composable
private fun KeyIcon(
    icon: ImageVector, onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val style = localKeyboardStyle.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = style.keyTextColor,
            modifier = Modifier.fillMaxSize(0.6f)   // 只佔 60%
        )
    }
}