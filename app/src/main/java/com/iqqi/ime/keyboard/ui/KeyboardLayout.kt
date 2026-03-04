package com.iqqi.ime.keyboard.ui


import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.iqqi.ime.keyboard.layout.englishLayout
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.model.KeyboardLanguage
import com.iqqi.ime.keyboard.state.KeyboardState
import com.iqqi.ime.keyboard.state.localKeyboardStyle

@Composable
fun KeyboardLayout(scale: Float, onKeyCommit: (KeySpec) -> Unit) {
    var state by remember { mutableStateOf(KeyboardState()) }

    val layout = when (state.language) {
        KeyboardLanguage.ENGLISH -> englishLayout
//        KeyboardLanguage.CHINESE -> chineseLayout
        else -> englishLayout
    }
    val style = localKeyboardStyle.current

    // 儲存每個按鍵相對於「鍵盤容器」的座標範圍
    val keyBounds = remember { mutableStateMapOf<KeySpec, Rect>() }
    var activeKey by remember { mutableStateOf<KeySpec?>(null) }

    // 用於捕捉根容器座標的變數
    var containerCoords by remember {
        mutableStateOf<androidx.compose.ui.layout.LayoutCoordinates?>(
            null
        )
    }

    // 獲取目前的螢幕密度與高度
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenHeightPx = context.resources.displayMetrics.heightPixels
    val keyboardHeightDp = with(density) { (screenHeightPx * scale).toDp() }
    val rowHeight = keyboardHeightDp / layout.size

    Box(
        modifier = Modifier
            .height(keyboardHeightDp)
            .fillMaxWidth()
            .background(style.backgroundColor)
            .graphicsLayer(clip = false)
            .onGloballyPositioned { containerCoords = it } // 捕捉父容器座標
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown()
                        val pointerId = down.id

                        // 內部搜尋函式
                        fun hitTest(position: Offset): KeySpec? {
                            return keyBounds.entries.firstOrNull {
                                it.value.contains(position)
                            }?.key
                        }

                        activeKey = hitTest(down.position)
                        down.consume()

                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                                ?: break // 失去追蹤則跳出

                            if (!change.pressed) {
                                activeKey?.let { onKeyCommit(it) }
                                activeKey = null
                                change.consume()
                                break
                            }

                            // 滑動過程中持續更新 activeKey (用於滑行輸入或校正)
                            activeKey = hitTest(change.position)
                            change.consume()
                        }
                    }
                }
            }
    ) {
        // 渲染按鍵
        Column {
            layout.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                ) {
                    row.forEach { key ->
                        KeyboardKey(
                            keyboardKey = key,
                            isActive = activeKey == key,
                            modifier = Modifier
                                .weight(key.weight)
                                .padding(2.dp)
                                .onGloballyPositioned { childCoords ->
                                    // 關鍵 2: 計算 child 相對於 container 的位置
                                    containerCoords?.let { parent ->
                                        val localOffset =
                                            parent.localPositionOf(childCoords, Offset.Zero)
                                        keyBounds[key] = Rect(
                                            offset = localOffset,
                                            size = androidx.compose.ui.geometry.Size(
                                                childCoords.size.width.toFloat(),
                                                childCoords.size.height.toFloat()
                                            )
                                        )
                                    }
                                })
                    }
                }
            }
        }

        // Preview Overlay (放在 Column 後面，確保在 Z 軸最上方)
        activeKey?.takeIf { it.type == KeyType.INPUT }?.let { key ->
            Log.d("zxc", "preview key = $key")
            val bounds = keyBounds[key]
            if (bounds != null) {
                KeyPreviewOverlay(
                    key = key,
                    keyBounds = bounds
                )
            }
        }
    }
}

@Composable
fun KeyboardKey(
    keyboardKey: KeySpec,
    isActive: Boolean,
    modifier: Modifier
) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .background(
                if (isActive) style.keyPressedColor
                else style.keyBackgroundColor,
                RoundedCornerShape(style.keyCornerRadius)
            )
            .border(
                1.dp,
                style.keyBorderColor,
                RoundedCornerShape(style.keyCornerRadius)
            )
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        val shortSidePx = with(density) {
            minOf(maxWidth.toPx(), maxHeight.toPx())
        }

        when (keyboardKey.type) {
            KeyType.INPUT -> {
                val label = keyboardKey.label ?: ""

                val fontSize = with(density) {
                    (shortSidePx * 0.45f).toSp()
                }

                Text(
                    text = label,
                    color = style.keyTextColor,
                    maxLines = 1,
                    fontSize = fontSize,
                )
            }

            else -> {
                Icon(
                    imageVector = keyboardKey.icon!!,
                    contentDescription = null,
                    tint = style.keyTextColor,
                    modifier = Modifier.fillMaxSize(0.6f)   // 只佔 60%
                )
            }
        }

    }
}

@Composable
fun KeyPreviewOverlay(
    key: KeySpec,
    keyBounds: Rect?
) {
    if (keyBounds == null) return

    val style = localKeyboardStyle.current
    val density = LocalDensity.current

    val bubbleWidth = keyBounds.width * 1.2f
    val bubbleHeight = keyBounds.height * 1.2f

    val shortSidePx = minOf(bubbleWidth, bubbleHeight)

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (keyBounds.left - (keyBounds.width * 0.1f)).toInt(),
                    (keyBounds.top - bubbleHeight - keyBounds.height * 0.1f).toInt()
                )
            }
            .size(
                with(LocalDensity.current) { bubbleWidth.toDp() },
                with(LocalDensity.current) { bubbleHeight.toDp() }
            )
            .background(
                style.keyPreviewedColor,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = with(density) {
            (shortSidePx * 0.6f).toSp()
        }

        Text(
            text = key.label ?: "",
            fontSize = fontSize,
            color = style.keyPreviewTextColor
        )
    }
}