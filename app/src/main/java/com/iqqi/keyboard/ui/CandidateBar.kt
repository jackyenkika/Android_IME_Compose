package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun CandidateBar(
    modifier: Modifier = Modifier,
    candidateFontScale: Float,
    candidates: List<String>,
    onCandidateClick: (Int) -> Unit,
) {
    val style = localKeyboardStyle.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val maxHeightPx = with(density) { maxHeight.toPx() } // BoxWithConstraints 提供 maxHeight
        val fontSize = with(density) { (maxHeightPx * candidateFontScale).toSp() }

        // 使用 LazyRow 或 Row 搭配 HorizontalScroll
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.Transparent), // 背景透明，顯示下層背景圖
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
        ) {
            itemsIndexed(candidates) { index, word ->
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp)
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null // 移除預設漣漪，或自訂
                        ) { onCandidateClick(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = word,
                        color = style.keyTextColor, // 或者獨立的 candidateTextColor
                        fontSize = fontSize,
                        maxLines = 1
                    )
                }

                // 候選字分隔線 (可選)
                if (index < candidates.lastIndex) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(20.dp)
                            .background(style.keyTextColor.copy(alpha = 0.2f))
                    )
                }
            }
        }
    }
}