package com.iqqi.keyboard.model

import android.net.Uri

data class Sticker(
    val id: String,
    val packId: String,
    val uri: Uri,
    val mimeType: String,
    val previewUri: Uri? = null,
    val width: Int = 0,
    val height: Int = 0,
    val isAnimated: Boolean = false
)

data class StickerPack(
    val id: String,
    val name: String,
    val iconUri: Uri,
    val stickers: List<Sticker>
)