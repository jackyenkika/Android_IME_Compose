package com.iqqi.ime.util

import android.view.inputmethod.InputConnection
import java.text.BreakIterator
import java.util.Locale

object DeleteObj {

    private val BLOCK_REGEX =
        Regex("""(\[[^\[\]]+])$|(【[^【】]+】)$|(<[^<>]+>)$""")

    private val PREFIX_REGEX =
        Regex("""(#\S+|\/:\S+)$""")

    private const val LOOKBACK = 100

    fun delete(ic: InputConnection) {

        val before = ic.getTextBeforeCursor(LOOKBACK, 0)?.toString() ?: ""

        if (before.isEmpty()) {
            ic.deleteSurroundingTextInCodePoints(1, 0)
            return
        }

        // 1 block macro
        BLOCK_REGEX.find(before)?.let {
            ic.deleteSurroundingText(it.value.length, 0)
            return
        }

        // 2 prefix macro
        PREFIX_REGEX.find(before)?.let {
            ic.deleteSurroundingText(it.value.length, 0)
            return
        }

        // 3 grapheme cluster (emoji / combined char)
        val cluster = lastGrapheme(before)
        if (cluster != null) {
            ic.deleteSurroundingText(cluster.length, 0)
            return
        }

        // fallback
        ic.deleteSurroundingTextInCodePoints(1, 0)
    }

    private fun lastGrapheme(text: String): String? {

        val iterator = BreakIterator.getCharacterInstance(Locale.getDefault())
        iterator.setText(text)

        val end = iterator.last()
        val start = iterator.previous()

        if (start == BreakIterator.DONE) return null

        return text.substring(start, end)
    }
}