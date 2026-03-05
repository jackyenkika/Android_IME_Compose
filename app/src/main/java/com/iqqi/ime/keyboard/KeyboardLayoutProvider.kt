package com.iqqi.ime.keyboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardCapslock
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material.icons.filled.Upgrade
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType
import com.iqqi.ime.keyboard.model.KeyboardMode
import com.iqqi.ime.keyboard.state.LayoutConfig
import com.iqqi.ime.keyboard.state.ShiftState

object KeyboardLayoutProvider {
    private val numberRow = "1234567890"
    private val letterRowsBase = listOf(
        "qwertyuiop",
        "asdfghjkl.",
        "zxcvbnm,"
    )

    val symbolPageCount
        get() = symbolPages.size
    private val symbolPages = listOf(

        listOf(
            "123~#@$%^&",
            "456?!,._\"\'",
            "789:;=-+*/",
            "[0]{}<>\\"
        ),

        listOf(
            "¡¿€¢£¥",
            "§©®™✓",
            "←↑→↓",
            "°•○●"
        )

    )

    fun create(config: LayoutConfig): List<List<KeySpec>> {

        val rows = mutableListOf<List<KeySpec>>()

        // 1️⃣ Number row
        if (config.showNumberRow && config.mode == KeyboardMode.LETTERS) {
            rows += createCharRow(numberRow, config, false)
        }

        // 2️⃣ Main rows
        when (config.mode) {

            KeyboardMode.LETTERS -> {
                letterRowsBase.forEach { row ->
                    rows += createCharRow(row, config, row == letterRowsBase.last())
                }
            }

            KeyboardMode.SYMBOLS -> {
                val page = config.pageIndex % symbolPages.size
                val targetSymbol = symbolPages[page]
                targetSymbol.forEach { row ->
                    rows += createCharRow(row, config, row == targetSymbol.last())
                }
            }
        }

        // 3️⃣ 功能列
        rows += createBottomRow(config)

        return rows
    }

    private fun createCharRow(
        chars: String,
        config: LayoutConfig,
        lastRow: Boolean
    ): List<KeySpec> {
        val rowKeys = mutableListOf<KeySpec>()
        if (lastRow && config.hasShift && config.mode == KeyboardMode.LETTERS) {
            rowKeys.add(
                KeySpec(
                    type = KeyType.SHIFT, icon =
                        if (config.shiftState == ShiftState.CAPS_LOCK)
                            Icons.Default.KeyboardCapslock
                        else if (config.shiftState == ShiftState.ON)
                            Icons.Default.Upgrade
                        else
                            Icons.Default.KeyboardArrowUp
                ),
            )
        }
        if (lastRow && config.mode == KeyboardMode.SYMBOLS) {
            rowKeys.add(
                KeySpec(
                    type = KeyType.NEXT_SYMBOL,
                    label = "${config.pageIndex + 1}/${symbolPages.size}"
                ),
            )
        }
        rowKeys.addAll(chars.map { c ->

            val finalChar = when (config.shiftState) {
                ShiftState.OFF -> c.lowercaseChar()
                ShiftState.ON,
                ShiftState.CAPS_LOCK -> c.uppercaseChar()
            }

            KeySpec(
                label = finalChar.toString(),
                code = finalChar.code,
                altChars = getAltChars(finalChar)
            )
        })
        if (lastRow) {
            rowKeys.add(
                KeySpec(
                    type = KeyType.DELETE,
                    icon = Icons.Default.Backspace,
                    isRepeatable = true
                )
            )

        }
        return rowKeys
    }

    private fun getAltChars(c: Char): List<String> {
        return when (c.lowercaseChar()) {
            'a' -> listOf("á", "à", "ä", "â", "ã")
            'e' -> listOf("é", "è", "ë", "ê")
            'i' -> listOf("í", "ì", "ï", "î")
            'o' -> listOf("ó", "ò", "ö", "ô", "õ")
            'u' -> listOf("ú", "ù", "ü", "û")
            else -> emptyList()
        }
    }

    private fun createBottomRow(
        config: LayoutConfig
    ): List<KeySpec> {
        return listOf(
            KeySpec(type = KeyType.CANCEL, icon = Icons.Default.ArrowDropDown),
            KeySpec(type = KeyType.SETTINGS, icon = Icons.Default.Settings),
            KeySpec(type = KeyType.LANGUAGE, icon = Icons.Default.Language),
            KeySpec(" ", 32, type = KeyType.SPACE, icon = Icons.Default.SpaceBar, weight = 5f),
            KeySpec(
                type = KeyType.SYMBOL,
                label = if (config.mode == KeyboardMode.LETTERS) "?123" else "ABC",
            ),
            KeySpec(type = KeyType.ENTER, icon = Icons.Default.KeyboardReturn)
        )
    }
}