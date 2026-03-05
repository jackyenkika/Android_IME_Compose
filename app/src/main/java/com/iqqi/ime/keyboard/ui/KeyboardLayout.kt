package com.iqqi.ime.keyboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.ui.window.Popup
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.state.localKeyboardStyle

@Composable
fun KeyboardLayout(scale: Float, layout: List<List<KeySpec>>, onKeyCommit: (KeySpec) -> Unit) {
    val style = localKeyboardStyle.current
    val context = LocalContext.current
    val density = LocalDensity.current

    // 狀態管理
    var activeKey by remember { mutableStateOf<KeySpec?>(null) }
    var containerSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var longPressActive by remember { mutableStateOf(false) }
    var altKeyIndex by remember { mutableStateOf(0) } // 選擇 altChars 索引

    // --- O(1) 關鍵：預計算每一行的 Weight 邊界比例 ---
    // 這樣觸碰時，只需要知道 X 在哪一個百分比區間就能抓到按鍵
    val rowSnapshots = remember(layout) {
        layout.map { row ->
            val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            var cumulative = 0f
            row.map {
                cumulative += it.weight / totalWeight
                cumulative // 存入累計比例，例如 [0.1, 0.2, 0.3...]
            }
        }
    }

    // 獲取目前的螢幕密度與高度
    val screenHeightPx = context.resources.displayMetrics.heightPixels
    val keyboardHeightDp = with(density) { (screenHeightPx * scale).toDp() }
    val rowHeight = keyboardHeightDp / layout.size

    // 核心 HitTest 函數
    fun findKeyAt(offset: Offset): KeySpec? {
        if (containerSize.height <= 0 || containerSize.width <= 0) return null

        // 1. 算出落在第幾行 (O(1))
        val rowIdx = (offset.y / containerSize.height * layout.size).toInt()
            .coerceIn(0, layout.size - 1)

        // 2. 算出落在該行第幾列 (O(k), k為單行按鍵數)
        val xRatio = offset.x / containerSize.width
        val colIdx = rowSnapshots[rowIdx].indexOfFirst { xRatio <= it }
            .let { if (it == -1) rowSnapshots[rowIdx].lastIndex else it }

        return layout[rowIdx][colIdx]
    }

    Box(
        modifier = Modifier
            .height(keyboardHeightDp)
            .fillMaxWidth()
            .background(style.backgroundColor)
            .graphicsLayer(clip = false)
            .onGloballyPositioned { containerSize = it.size } // 捕捉父容器座標
            .pointerInput(layout, containerSize) {

                awaitEachGesture {

                    val down = awaitFirstDown()
                    activeKey = findKeyAt(down.position)

                    altKeyIndex = 0
                    longPressActive = false

                    val longPress = awaitLongPressOrCancellation(down.id)

                    // 如果成功觸發長按
                    if (longPress != null && activeKey?.altChars?.isNotEmpty() == true) {

                        longPressActive = true

                        drag(longPress.id) { change ->

                            val key = activeKey ?: return@drag
                            val altChars = key.altChars

                            if (altChars.isEmpty()) return@drag

                            val rowIdx = layout.indexOfFirst { it.contains(key) }
                            val colIdx = layout[rowIdx].indexOf(key)

                            val prevWeightSum =
                                layout[rowIdx].take(colIdx).sumOf { it.weight.toDouble() }.toFloat()

                            val totalWeight =
                                layout[rowIdx].sumOf { it.weight.toDouble() }.toFloat()

                            val keyWidth = containerSize.width * (key.weight / totalWeight)
                            val keyLeft = containerSize.width * (prevWeightSum / totalWeight)

                            val localX = change.position.x - keyLeft
                            val cellWidth = keyWidth / altChars.size

                            altKeyIndex =
                                (localX / cellWidth).toInt().coerceIn(0, altChars.lastIndex)

                            change.consume()
                        }

                        // 手指放開
                        val selected = activeKey?.altChars?.getOrNull(altKeyIndex)

                        if (selected != null) {
                            onKeyCommit(activeKey!!.copy(label = selected))
                        } else {
                            activeKey?.let { onKeyCommit(it) }
                        }

                        activeKey = null
                        longPressActive = false

                    } else {

                        // 一般 tap / slide typing
                        drag(down.id) { change ->

                            activeKey = findKeyAt(change.position)

                            change.consume()
                        }

                        activeKey?.let { onKeyCommit(it) }

                        activeKey = null
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
                        )
                    }
                }
            }
        }

        // Preview Overlay (放在 Column 後面，確保在 Z 軸最上方)
        activeKey?.takeIf { it.type == KeyType.INPUT }?.let { key ->
            // 由於沒有 keyBounds Map 了，我們直接計算 Overlay 應該出現的坐標
            val rowIdx = layout.indexOfFirst { it.contains(key) }
            val colIdx = layout[rowIdx].indexOf(key)
            val prevWeightSum = layout[rowIdx].take(colIdx).sumOf { it.weight.toDouble() }.toFloat()
            val totalWeight = layout[rowIdx].sumOf { it.weight.toDouble() }.toFloat()

            // 計算該按鍵在 Box 內的 Rect
            val keyWidth = containerSize.width * (key.weight / totalWeight)
            val keyLeft = containerSize.width * (prevWeightSum / totalWeight)
            val keyTop = (containerSize.height / layout.size) * rowIdx
            val rect = Rect(
                keyLeft,
                keyTop.toFloat(),
                keyLeft + keyWidth,
                (keyTop + (containerSize.height / layout.size)).toFloat()
            )

            if (longPressActive && key.altChars.isNotEmpty()) {
                AltCharsPreviewOverlay(key, rect, altKeyIndex)
            } else {
                KeyPreviewOverlay(label = key.label ?: "", keyBounds = rect)
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
    label: String,
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
                text = label,
                fontSize = with(density) { (shortSidePx * 0.5f).toSp() },
                color = style.keyPreviewTextColor
            )
        }
    }
}

@Composable
fun AltCharsPreviewOverlay(key: KeySpec, keyBounds: Rect, selectedIndex: Int) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current
    val altChars = key.altChars
    val cellWidth = keyBounds.width / altChars.size
    val cellHeight = keyBounds.height * 1.5f

    Popup(
        offset = IntOffset(
            keyBounds.left.toInt(),
            (keyBounds.top - cellHeight).toInt()
        )
    ) {
        Row(
            modifier = Modifier
                .size(
                    width = with(density) { keyBounds.width.toDp() },
                    height = with(density) { cellHeight.toDp() }
                )
                .background(style.keyPreviewedColor, RoundedCornerShape(8.dp))
        ) {
            altChars.forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (index == selectedIndex) style.keyPressedColor
                            else style.keyPreviewedColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = with(density) { (cellHeight * 0.5f).toSp() },
                        color = style.keyPreviewTextColor
                    )
                }
            }
        }
    }
}