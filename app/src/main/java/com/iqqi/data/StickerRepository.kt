package com.iqqi.data

import android.content.Context
import com.iqqi.keyboard.model.Sticker
import com.iqqi.keyboard.model.StickerPack

class StickerRepository(val context: Context) {

    private val recent = mutableListOf<Sticker>()
    private val favorites = mutableSetOf<String>()

    fun getStickerPacks(): List<StickerPack> {
        return loadFromAssets(context)
    }

    fun addRecent(sticker: Sticker) {
        recent.removeAll { it.id == sticker.id }
        recent.add(0, sticker)

        if (recent.size > 30) recent.removeLast()
    }

    fun getRecent(): List<Sticker> = recent

    fun toggleFavorite(id: String) {
        if (!favorites.add(id)) favorites.remove(id)
    }

    fun isFavorite(id: String) = id in favorites

    private fun loadFromAssets(context: Context): List<StickerPack> {

        val am = context.assets
        val packs = mutableListOf<StickerPack>()

        val root = "stickers"
        val packNames = am.list(root) ?: return emptyList()

        for (pack in packNames) {

            val files = am.list("$root/$pack") ?: continue

            val stickers = files
                .filter { it != "icon.png" }
                .map { file ->

                    val uri = android.net.Uri.parse(
                        "file:///android_asset/$root/$pack/$file"
                    )

                    Sticker(
                        id = "$pack/$file",
                        packId = pack,
                        uri = uri,
                        mimeType = when {
                            file.endsWith(".gif") -> "image/gif"
                            file.endsWith(".webp") -> "image/webp"
                            else -> "image/png"
                        }
                    )
                }

            val iconUri = android.net.Uri.parse(
                "file:///android_asset/$root/$pack/icon.png"
            )

            packs.add(
                StickerPack(
                    id = pack,
                    name = pack,
                    iconUri = iconUri,
                    stickers = stickers
                )
            )
        }

        return packs
    }
}