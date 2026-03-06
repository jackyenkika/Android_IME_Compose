package com.iqqi.ime.keyboard.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.state.localKeyboardStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun KeyboardLayout(
    keyboardHeightDp: Dp,
    layout: List<List<KeySpec>>,
    candidates: List<String> = emptyList(),
    onCandidateClick: (String) -> Unit,
    onKeyCommit: (KeySpec) -> Unit
) {
    val style = localKeyboardStyle.current

    // 狀態管理
    val scope = rememberCoroutineScope() // 1. 取得外部 Scope
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

    val candidateBarHeight = 48.dp
    val rowHeight = (keyboardHeightDp - candidateBarHeight) / layout.size

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
    ) {
        // 1️⃣ 背景圖片
        style.backgroundImage?.let { painter ->
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
        }

        // 2️⃣ tint / blur layer (可選)
        Box(
            Modifier
                .fillMaxSize()
                .background(style.backgroundColor)
        )

        // 3️⃣ 內容層 (CandidateBar + 鍵盤按鍵)
        Column(modifier = Modifier.fillMaxSize()) {

            // 候選字欄位
            CandidateBar(
                candidates = candidates,
                onCandidateClick = onCandidateClick,
                modifier = Modifier.height(candidateBarHeight)
            )

            Box(
                modifier = Modifier
                    .graphicsLayer(clip = false)
                    .onGloballyPositioned { containerSize = it.size } // 捕捉父容器座標
                    .pointerInput(layout, containerSize) {

                        awaitEachGesture {

                            val down = awaitFirstDown()
                            activeKey = findKeyAt(down.position)

                            altKeyIndex = 0
                            longPressActive = false
                            var repeatJob: kotlinx.coroutines.Job? = null
                            var wasLongPressed = false

                            // 1. 啟動 Repeat Job (如果是 repeatable)
                            if (activeKey?.isRepeatable == true) {
                                // 使用外部 scope 啟動，而不是直接呼叫 launch
                                repeatJob = scope.launch {
                                    // 先執行第一次按下 (立即反應)
                                    onKeyCommit(activeKey!!)
                                    delay(400) // 第一次重複前的長延遲
                                    while (true) {
                                        activeKey?.let { onKeyCommit(it) }
                                        delay(60)
                                    }
                                }
                            }

                            // 偵測長按
                            val longPress = awaitLongPressOrCancellation(down.id)
                            // 如果成功觸發長按
                            if (longPress != null && activeKey?.altChars?.isNotEmpty() == true) {
                                wasLongPressed = true
                                repeatJob?.cancel()
                                longPressActive = true

                                drag(longPress.id) { change ->

                                    val key = activeKey ?: return@drag
                                    val altChars = key.altChars

                                    if (altChars.isEmpty()) return@drag

                                    val rowIdx = layout.indexOfFirst { it.contains(key) }
                                    val colIdx = layout[rowIdx].indexOf(key)

                                    val prevWeightSum =
                                        layout[rowIdx].take(colIdx).sumOf { it.weight.toDouble() }
                                            .toFloat()

                                    val totalWeight =
                                        layout[rowIdx].sumOf { it.weight.toDouble() }.toFloat()

                                    val keyWidth = containerSize.width * (key.weight / totalWeight)
                                    val keyLeft =
                                        containerSize.width * (prevWeightSum / totalWeight)

                                    val localX = change.position.x - keyLeft
                                    val cellWidth = keyWidth / altChars.size

                                    altKeyIndex =
                                        (localX / cellWidth).toInt().coerceIn(0, altChars.lastIndex)

                                    change.consume()
                                }

                                // 抬起後送出 AltChar
                                activeKey?.altChars?.getOrNull(altKeyIndex)?.let {
                                    onKeyCommit(activeKey!!.copy(label = it))
                                }
                            } else {
                                // 如果不是 repeatable，我們就在這裡處理單次 tap 的 commit
                                if (activeKey?.isRepeatable != true) {
                                    // 這裡暫時不送出，等抬起時確定沒滑走再送，或者按下立即送
                                }

                                // 一般 tap / slide typing
                                drag(down.id) { change ->
                                    val currentKey = findKeyAt(change.position)
                                    // 如果滑出了原本的 repeatable 按鍵，取消 Job
                                    if (currentKey != activeKey) {
                                        repeatJob?.cancel()
                                        activeKey = currentKey
                                    }
                                    change.consume()
                                }
                                // 抬起後的處理：如果不是長按，且不是 repeatable (因為 repeatable 已經在 Job 處理了)
                                // 或者是 repeatable 但 Job 還沒跑過第一次延遲就放開了
                                if (!wasLongPressed && activeKey?.isRepeatable != true) {
                                    activeKey?.let { onKeyCommit(it) }
                                }
                            }
                            // 最後一定要確保 Job 被取消，防止手指離開後還在噴字
                            repeatJob?.cancel()
                            activeKey = null
                            longPressActive = false
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
                    val prevWeightSum =
                        layout[rowIdx].take(colIdx).sumOf { it.weight.toDouble() }.toFloat()
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
fun AltCharsPreviewOverlay(
    key: KeySpec,
    keyBounds: Rect,
    selectedIndex: Int
) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current
    val altChars = key.altChars
    if (altChars.isEmpty()) return

    // 配置參數
    val cellWidthDp = 48.dp // 每個候選字的基本寬度
    val cellHeightDp = 56.dp // 每個候選字的高度，略高於一般按鍵
    val verticalOffsetDp = 8.dp // 與原按鍵上緣的間距

    // 計算總寬度
    val totalWidthPx = with(density) { (cellWidthDp * altChars.size).toPx() }
    val cellHeightPx = with(density) { cellHeightDp.toPx() }
    val verticalOffsetPx = with(density) { verticalOffsetDp.toPx() }

    // 計算 X 座標：盡量置中於按鍵，但避免超出螢幕 (0 到 screenWidth)
    // 注意：這裡假設 keyBounds 是相對於鍵盤容器的
    val preferredX = keyBounds.center.x - (totalWidthPx / 2)
    val popupX = preferredX.coerceIn(0f, Float.MAX_VALUE) // 這裡可用 LocalConfiguration 取得寬度做更嚴謹限制

    // 計算 Y 座標：按鍵頂部 - 選單高度 - 間距
    val popupY = keyBounds.top - cellHeightPx - verticalOffsetPx

    Popup(
        offset = IntOffset(popupX.toInt(), popupY.toInt()),
        properties = androidx.compose.ui.window.PopupProperties(
            focusable = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // 使用 Row 建立一個連續的面板
        Row(
            modifier = Modifier
                .background(
                    color = style.keyPreviewedColor, // 面板背景色
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 0.5.dp,
                    color = style.keyBorderColor.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp), // 內邊距讓選取的 Highlight 不會貼邊
            verticalAlignment = Alignment.CenterVertically
        ) {
            altChars.forEachIndexed { index, label ->
                val isSelected = index == selectedIndex

                Box(
                    modifier = Modifier
                        .size(cellWidthDp, cellHeightDp)
                        .background(
                            if (isSelected) style.keyPressedColor else androidx.compose.ui.graphics.Color.Transparent,
                            RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = style.keyPreviewTextColor,
                        fontSize = with(density) { (cellHeightPx * 0.4f).toSp() },
                        style = androidx.compose.ui.text.TextStyle(
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.ExtraBold else androidx.compose.ui.text.font.FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}