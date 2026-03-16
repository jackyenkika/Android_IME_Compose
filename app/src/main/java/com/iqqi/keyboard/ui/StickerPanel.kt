package com.iqqi.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iqqi.keyboard.model.Sticker
import com.iqqi.keyboard.model.StickerPack

@Composable
fun StickerPanel(
    packs: List<StickerPack>,
    onStickerClick: (Sticker) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPack by remember {
        mutableStateOf(packs.firstOrNull())
    }

    Column(
        modifier = modifier   // ⭐ 加上 modifier
    ) {

//        PackTabs(packs) {
//            selectedPack = it
//        }

        selectedPack?.let { pack ->
            StickerGrid(
                pack.stickers,
                onStickerClick,
                modifier = Modifier.weight(1f)  // ⭐ 關鍵
            )
        }
    }
}

@Composable
fun PackTabs(
    packs: List<StickerPack>,
    onSelect: (StickerPack) -> Unit = {}
) {

    LazyRow(
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 0.dp)
    ) {
        items(packs) { pack ->

            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(32.dp)
                    .clickable { onSelect(pack) },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = pack.iconUri,
                    contentDescription = pack.name
                )
            }
        }
    }
}

@Composable
fun StickerGrid(
    stickers: List<Sticker>,
    onClick: (Sticker) -> Unit,
    modifier: Modifier = Modifier
) {

    val style = localKeyboardStyle.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier   // ⭐ 關鍵
    ) {
        items(stickers) { sticker ->
            AsyncImage(
                model = sticker.previewUri ?: sticker.uri,
                contentDescription = null,
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .background(style.keyBackgroundColor)
                    .clickable { onClick(sticker) }
            )
        }
    }
}