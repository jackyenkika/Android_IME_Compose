package com.iqqi.ime.util

import android.view.inputmethod.InputConnection
import java.text.BreakIterator
import java.util.Locale

object DeleteObj {

    private const val LOOKBACK = 32

    // ICU iterator (避免每次 new)
    private val iterator =
        BreakIterator.getCharacterInstance(Locale.getDefault())

    // block macro
    private val BLOCK_REGEX =
        Regex("""(\[[^\[\]]+])$|(【[^【】]+】)$|(<[^<>]+>)$""")

    // prefix macro
    private val PREFIX_REGEX =
        Regex("""(#\S+|\/:\S+)$""")

    fun delete(ic: InputConnection) {

        val before = ic.getTextBeforeCursor(LOOKBACK, 0)?.toString() ?: ""

        if (before.isEmpty()) {
            ic.deleteSurroundingTextInCodePoints(1, 0)
            return
        }

        val last = before.last()

        // ------------------------------------------------
        // 1️⃣ FAST PATH (ASCII)
        // ------------------------------------------------

        if (last.code < 128 && last.isLetterOrDigit()) {
            ic.deleteSurroundingText(1, 0)
            return
        }

        // ------------------------------------------------
        // 2️⃣ BLOCK MACRO
        // ------------------------------------------------

        if (last == ']' || last == '】' || last == '>') {

            BLOCK_REGEX.find(before)?.let {
                ic.deleteSurroundingText(it.value.length, 0)
                return
            }
        }

        // ------------------------------------------------
        // 3️⃣ PREFIX MACRO
        // ------------------------------------------------

        if (last == ':' || last == ' ') {

            PREFIX_REGEX.find(before)?.let {
                ic.deleteSurroundingText(it.value.length, 0)
                return
            }
        }

        // ------------------------------------------------
        // 4️⃣ EMOJI / GRAPHEME CLUSTER
        // ------------------------------------------------

        val cluster = lastGrapheme(before)

        if (cluster != null && cluster.length > 1) {
            ic.deleteSurroundingText(cluster.length, 0)
            return
        }

        // ------------------------------------------------
        // 5️⃣ FALLBACK
        // ------------------------------------------------

        ic.deleteSurroundingTextInCodePoints(1, 0)
    }

    private fun lastGrapheme(text: String): String? {

        iterator.setText(text)

        val end = iterator.last()
        val start = iterator.previous()

        if (start == BreakIterator.DONE) return null

        return text.substring(start, end)
    }
}