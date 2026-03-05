package com.iqqi.ime.keyboard.ui

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.state.localKeyboardStyle

@Composable
fun KeyboardLayout(scale: Float, layout: List<List<KeySpec>>, onKeyCommit: (KeySpec) -> Unit) {
    val style = localKeyboardStyle.current

    // 儲存每個按鍵相對於「鍵盤容器」的座標範圍
    val keyBounds = remember(layout) { mutableMapOf<KeySpec, Rect>() }
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
            .pointerInput(layout) {
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
                                    val parent = containerCoords ?: return@onGloballyPositioned
                                    // 直接存入 MutableMap，不觸發狀態更新
                                    val localOffset =
                                        parent.localPositionOf(childCoords, Offset.Zero)
                                    keyBounds[key] = Rect(
                                        offset = localOffset,
                                        size = childCoords.size.toSize()
                                    )
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

@SuppressLint("UnusedBoxWithConstraintsScope")
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
        // 取得當前容器的最短邊
        val shortSideDp = minOf(maxWidth, maxHeight)

        // 根據類型決定比例
        val multiplier = when (keyboardKey.type) {
            KeyType.INPUT -> 0.45f
            KeyType.SYMBOL, KeyType.NEXT_SYMBOL -> 0.35f
            else -> 1.0f // Icon 類型
        }

        when (keyboardKey.type) {
            KeyType.INPUT, KeyType.SYMBOL, KeyType.NEXT_SYMBOL -> {
                val label = keyboardKey.label ?: ""
                val fontSize = with(density) { (shortSideDp.toPx() * multiplier).toSp() }

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
    keyBounds: Rect
) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current

    val bubbleWidth = keyBounds.width * 1.2f
    val bubbleHeight = keyBounds.height * 1.2f
    val shortSidePx = minOf(bubbleWidth, bubbleHeight)

    Popup(
        offset = IntOffset(
            (keyBounds.left + (keyBounds.width - shortSidePx) / 2).toInt(),
            (keyBounds.top - shortSidePx * 1.1f).toInt()
        )
    ) {

        Box(
            modifier = Modifier
                .size(
                    with(density) { shortSidePx.toDp() },
                    with(density) { shortSidePx.toDp() }
                )
                .background(
                    style.keyPreviewedColor,
                    RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = key.label ?: "",
                fontSize = with(density) { (shortSidePx * 0.5f).toSp() },
                color = style.keyPreviewTextColor
            )
        }
    }
}