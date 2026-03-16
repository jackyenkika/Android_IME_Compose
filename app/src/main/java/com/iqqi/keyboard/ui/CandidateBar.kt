package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.iqqi.keyboard.model.KeySpec

@Composable
fun CandidateBar(
    modifier: Modifier = Modifier,
    candidateFontScale: Float,
    candidates: List<String>,
    functions: List<KeySpec>,
    onCandidateClick: (Int) -> Unit,
    onFunctionClick: (KeySpec) -> Unit
) {
    val style = localKeyboardStyle.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(style.candidateBackgroundColor)
    ) {
        if (candidates.isEmpty()) {
            FunctionRow(functions, onFunctionClick)
        } else {
            val density = LocalDensity.current
            val maxHeightPx = with(density) { maxHeight.toPx() } // BoxWithConstraints 提供 maxHeight
            val fontSize = with(density) { (maxHeightPx * candidateFontScale).toSp() }
            CandidateRow(candidates, fontSize, onCandidateClick)
        }
    }
}

@Composable
private fun CandidateRow(
    candidates: List<String>,
    fontSize: TextUnit,
    onCandidateClick: (Int) -> Unit
) {
    val style = localKeyboardStyle.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(candidates) { index, word ->

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp)
                    .clickable { onCandidateClick(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = word,
                    color = style.candidateTextColor,
                    fontSize = fontSize,
                    maxLines = 1
                )
            }

            if (index < candidates.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(style.keyTextColor.copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
private fun FunctionRow(
    functions: List<KeySpec>,
    onFunctionClick: (KeySpec) -> Unit
) {
    val style = localKeyboardStyle.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(functions) { func ->

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp)
                    .clickable { onFunctionClick(func) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = func.icon!!,
                    contentDescription = null,
                    tint = style.keyTextColor,
                    modifier = Modifier.fillMaxSize(0.6f)   // 只佔 60%
                )
            }
        }
    }
}