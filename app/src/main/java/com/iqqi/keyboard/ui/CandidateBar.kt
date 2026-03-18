package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.iqqi.keyboard.model.KeySpec

@Composable
fun CandidateBar(
    modifier: Modifier = Modifier,
    candidateFontScale: Float,
    candidates: List<String>,
    specialCandidates: Set<String>,
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
            CandidateRow(candidates, fontSize, specialCandidates, onCandidateClick)
        }
    }
}

@Composable
private fun CandidateRow(
    candidates: List<String>,
    fontSize: TextUnit,
    specialCandidates: Set<String>,
    onCandidateClick: (Int) -> Unit
) {
    val style = localKeyboardStyle.current

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        itemsIndexed(candidates) { index, word ->

            val isSpecial = specialCandidates.contains(word.lowercase())

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(
                        brush = Brush.verticalGradient(if (isSpecial) style.candidateSpecialTextBackgroundColor else style.candidateTextBackgroundColor),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // 禁用 ripple
                    ) { onCandidateClick(index) }, contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = word,
                    color = if (isSpecial) style.candidateSpecialTextColor else style.candidateTextColor,
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
    functions: List<KeySpec>, onFunctionClick: (KeySpec) -> Unit
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
                    .then(
                        if (func.isEnable) {
                            Modifier.clickable { onFunctionClick(func) }
                        } else Modifier // 不可點擊
                    ), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = func.icon!!,
                    contentDescription = null,
                    tint = if (func.isEnable) style.keyTextColor else style.keyTextColor.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize(0.6f)   // 只佔 60%
                )
            }
        }
    }
}