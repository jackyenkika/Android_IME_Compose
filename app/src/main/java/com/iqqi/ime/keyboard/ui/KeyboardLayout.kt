package com.iqqi.ime.keyboard.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iqqi.ime.IMEService
import com.iqqi.ime.keyboard.state.localKeyboardStyle

@Composable
fun KeyboardLayout(scale: Float) {
    val keysMatrix = arrayOf(
        arrayOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        arrayOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        arrayOf("Z", "X", "C", "V", "B", "N", "M")
    )
    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels
    val keyboardHeight = (screenHeight * scale).dp
    val rowHeight = keyboardHeight / keysMatrix.size

    val style = localKeyboardStyle.current
    Column(
        modifier = Modifier
            .background(style.backgroundColor)
            .height(keyboardHeight)
            .fillMaxWidth()
    ) {
        keysMatrix.forEach { row ->
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
    keyboardKey: String,
    modifier: Modifier
) {
    val style = localKeyboardStyle.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed = interactionSource.collectIsPressedAsState()
    val ctx = LocalContext.current
    Box(
        modifier = modifier
            .padding(2.dp)
            .background(
                if (pressed.value)
                    style.keyPressedColor else style.keyBackgroundColor,
                RoundedCornerShape(style.keyCornerRadius)
            )
            .border(1.dp, style.keyBorderColor, RoundedCornerShape(style.keyCornerRadius))
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Text(
            keyboardKey,
            Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .clickable(interactionSource = interactionSource, indication = null) {
                    (ctx as IMEService).currentInputConnection.commitText(
                        keyboardKey,
                        keyboardKey
                            .length
                    )
                }
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 16.dp,
                    bottom = 16.dp
                )

        )
    }
}