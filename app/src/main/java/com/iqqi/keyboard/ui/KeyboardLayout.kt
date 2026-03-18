package com.iqqi.keyboard.ui

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.iqqi.ime.R
import com.iqqi.keyboard.model.KeySpec
import com.iqqi.keyboard.model.KeyType
import com.iqqi.keyboard.model.KeyboardThemeSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class RowMetrics(
    val widthRatio: Float, val leftRatio: Float
)

data class DeviceConfig(
    val keyboardHeight: Dp = 400.dp,
    val candidateHeight: Dp = 44.dp,
    val candidateFontScale: Float = 0.9f
)

data class AnimationConfig(
    val shakeOffset: Float = 0f,
    val showAnimation: Boolean = false
)

data class OverlayConfig(
    val alpha: Float = 0f,
    val consumeTouch: Boolean = true
)

val localKeyboardStyle = staticCompositionLocalOf<KeyboardThemeSpec> {
    error("No KeyboardStyle provided")
}

@Composable
fun KeyboardLayout(
    deviceConfig: DeviceConfig,
    layout: List<List<KeySpec>>,
    candidates: List<String> = emptyList(),
    candidateFunctions: List<KeySpec> = emptyList(),
    specialCandidates: Set<String> = emptySet(),
    animationConfig: AnimationConfig,
    overlay: (@Composable () -> Unit)? = null,
    overlayConfig: OverlayConfig,
    onAnimationEnd: () -> Unit,
    onDeleteUp: () -> Unit,
    onCandidateClick: (Int) -> Unit,
    onKeyCommit: (KeySpec) -> Unit,
) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current

    // 狀態管理
    val scope = rememberCoroutineScope() // 1. 取得外部 Scope
    var activeKey by remember { mutableStateOf<KeySpec?>(null) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
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

    val candidateBarHeight = deviceConfig.candidateHeight
    val navigationInsetPx = WindowInsets.systemBars.getBottom(density)
    val visualKeyboardHeight =
        deviceConfig.keyboardHeight - with(density) { navigationInsetPx.toDp() }

    val rowHeight = (visualKeyboardHeight - candidateBarHeight) / layout.size


    val maxWeight = layout.maxOf { row ->
        row.sumOf { it.weight.toDouble() }.toFloat()
    }

    val rowMetrics = remember(layout) {
        layout.map { row ->
            val rowWeight = row.sumOf { it.weight.toDouble() }.toFloat()
            val widthRatio = rowWeight / maxWeight
            val leftRatio = (1f - widthRatio) / 2f
            RowMetrics(widthRatio, leftRatio)
        }
    }

    val confettiImages = remember {
        listOf(
            R.drawable.img_gold_confetti_1,
            R.drawable.img_gold_confetti_2,
            R.drawable.img_gold_confetti_3,
            R.drawable.img_gold_confetti_4,
            R.drawable.img_gold_confetti_5,
            R.drawable.img_gold_confetti_6,
            R.drawable.img_gold_confetti_7,
            R.drawable.img_gold_confetti_8,
            R.drawable.img_gold_confetti_9,
            R.drawable.img_gold_confetti_10,
            R.drawable.img_gold_confetti_11,
            R.drawable.img_gold_confetti_12,
        )
    }.map { painterResource(it) }

    val footballImages =
        remember {
            listOf(R.drawable.img_soccer)
        }.map { painterResource(it) }

    // 核心 HitTest 函數
    fun findKeyAt(offset: Offset): KeySpec? {
        if (containerSize.height <= 0 || containerSize.width <= 0) return null

        // 1. 算出落在第幾行 (O(1))
        val rowIdx =
            (offset.y / containerSize.height * layout.size).toInt().coerceIn(0, layout.size - 1)

        // 2. 算出落在該行第幾列 (O(k), k為單行按鍵數)
        val metrics = rowMetrics[rowIdx]

        val xRatioGlobal = offset.x / containerSize.width

        val xRatio = ((xRatioGlobal - metrics.leftRatio) / metrics.widthRatio)
        if (xRatio !in 0f..1f) return null

        val colIdx = rowSnapshots[rowIdx].indexOfFirst { xRatio <= it }
            .let { if (it == -1) rowSnapshots[rowIdx].lastIndex else it }

        return layout[rowIdx][colIdx]
    }

    Box(
        modifier = Modifier
            .height(deviceConfig.keyboardHeight)
            .fillMaxWidth()
            .graphicsLayer {
                translationX = animationConfig.shakeOffset
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.systemBars.only(WindowInsetsSides.Bottom)
                )
        ) {

            // 候選字欄位
            CandidateBar(
                modifier = Modifier.height(candidateBarHeight),
                candidateFontScale = deviceConfig.candidateFontScale,
                candidates = candidates,
                specialCandidates = specialCandidates,
                functions = candidateFunctions,
                onCandidateClick = onCandidateClick,
                onFunctionClick = onKeyCommit,
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
                            var repeatJob: Job? = null
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
                                    if (rowIdx == -1) return@drag

                                    val colIdx = layout[rowIdx].indexOf(key)
                                    if (colIdx == -1) return@drag

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

                            if (activeKey?.type == KeyType.DELETE) {
                                onDeleteUp()
                            }

                            activeKey = null
                            longPressActive = false
                        }
                    }) {
                // 渲染按鍵
                Column {
                    layout.forEachIndexed { index, row ->
                        val rowWeight = row.sumOf { it.weight.toDouble() }.toFloat()

                        val widthRatio = if (index == layout.lastIndex) 1f
                        else rowWeight / maxWeight

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(widthRatio)
                                .align(Alignment.CenterHorizontally)
                                .height(rowHeight)
                        ) {
                            row.forEach { key ->
                                KeyboardKey(
                                    keyboardKey = key,
                                    isActive = activeKey == key,
                                    shakeOffset = animationConfig.shakeOffset,
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
                    if (rowIdx == -1) return@let   // 或 return
                    val colIdx = layout[rowIdx].indexOf(key)
                    if (colIdx == -1) return@let

                    val prevWeightSum =
                        layout[rowIdx].take(colIdx).sumOf { it.weight.toDouble() }.toFloat()
                    val totalWeight = layout[rowIdx].sumOf { it.weight.toDouble() }.toFloat()

                    val metrics = rowMetrics[rowIdx]

                    val rowWidth = containerSize.width * metrics.widthRatio
                    val rowLeft = containerSize.width * metrics.leftRatio

                    // 計算該按鍵在 Box 內的 Rect
                    val keyWidth = rowWidth * (key.weight / totalWeight)
                    val keyLeft = rowLeft + rowWidth * (prevWeightSum / totalWeight)
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

                overlay?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(style.backgroundColor.copy(alpha = overlayConfig.alpha)) // 可視暗背景
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    // 只 consume down
                                    down.consume()

                                    // 允許 drag / move 向下傳遞
                                    drag(down.id) { change ->
                                        // 不 consume change
                                    }
                                }
                            }
                    ) {
                        it()
                    }
                }
            }
        }
        if (animationConfig.showAnimation) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                FootballGoalAnimation(
                    confettiImages = confettiImages,
                    footballImages = footballImages
                ) {
                    // 動畫結束後的回調，目前不需要特別處理
                    onAnimationEnd()
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun KeyboardKey(
    keyboardKey: KeySpec, isActive: Boolean, shakeOffset: Float, modifier: Modifier
) {
    val style = localKeyboardStyle.current
    val density = LocalDensity.current


    val bgColor = when (keyboardKey.type) {
        KeyType.INPUT, KeyType.SPACE -> style.keyBackgroundColor
        KeyType.ENTER -> style.keyPrimaryBackgroundColor
        else -> style.keyFunctionalBackgroundColor
    }

    val textColor = when (keyboardKey.type) {
        KeyType.INPUT, KeyType.SPACE -> style.keyTextColor
        KeyType.ENTER -> style.keyPrimaryTextColor
        else -> style.keyFunctionalTextColor
    }

    val scale = if (
        keyboardKey.type == KeyType.ENTER &&
        shakeOffset < 0f
    ) 0.9f else 1f

    BoxWithConstraints(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                if (isActive) style.keyPressedColor
                else bgColor, RoundedCornerShape(style.keyCornerRadius)
            )
            .border(
                1.5.dp, style.keyBorderColor, RoundedCornerShape(style.keyCornerRadius)
            )
            .fillMaxHeight(), contentAlignment = Alignment.Center
    ) {
        // 取得當前容器的最短邊
        val shortSideDp = minOf(maxWidth, maxHeight)

        // 根據類型決定比例
        val multiplier = when (keyboardKey.type) {
            KeyType.INPUT -> 0.6f
            KeyType.SYMBOL, KeyType.NEXT_SYMBOL -> 0.3f
            KeyType.SPACE -> 0.2f
            else -> 1.0f // Icon 類型
        }

        when (keyboardKey.type) {
            KeyType.INPUT,
            KeyType.SYMBOL,
            KeyType.NEXT_SYMBOL,
            KeyType.SPACE -> {
                val label = keyboardKey.label ?: ""
                val fontSize = with(density) { (shortSideDp.toPx() * multiplier).toSp() }

                Text(
                    text = label,
                    color = textColor,
                    maxLines = 1,
                    fontWeight = FontWeight.W500,
                    fontSize = fontSize,
                )
            }

            else -> {
                val painter = rememberPainterForKey(keyboardKey)

                if (painter != null) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.fillMaxSize(0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberPainterForKey(key: KeySpec): Painter? {

    // 優先使用 drawable（且未過期）
    key.iconDrawable
        ?.takeIf { it.resId != null && !it.isExpired() }
        ?.let { return painterResource(it.resId!!) }

    // fallback 到 ImageVector
    key.icon?.let { return rememberVectorPainter(it) }

    return null
}

@Composable
fun KeyPreviewOverlay(
    label: String, keyBounds: Rect
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
                    with(density) { shortSidePx.toDp() })
                .background(
                    style.keyPreviewedColor, RoundedCornerShape(50.dp)
                ), contentAlignment = Alignment.Center
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
    key: KeySpec, keyBounds: Rect, selectedIndex: Int
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
        properties = PopupProperties(
            focusable = false, dismissOnBackPress = true, dismissOnClickOutside = true
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
                            if (isSelected) style.keyPressedColor else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        ), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = style.keyPreviewTextColor,
                        fontSize = with(density) { (cellHeightPx * 0.4f).toSp() },
                        style = TextStyle(
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}