package com.iqqi.keyboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.SpaceBar
import com.iqqi.ime.BuildConfig
import com.iqqi.ime.R
import com.iqqi.keyboard.model.IconImage
import com.iqqi.keyboard.model.KeySpec
import com.iqqi.keyboard.model.KeyType
import com.iqqi.keyboard.model.KeyboardLanguage
import com.iqqi.keyboard.model.KeyboardMode
import com.iqqi.keyboard.state.LayoutConfig
import com.iqqi.keyboard.state.ShiftState

object KeyboardLayoutProvider {
    private val numberRow = "1234567890"
    private val letterRowsBase = listOf(
        "qwertyuiop", "asdfghjkl", "zxcvbnm"
    )

    val symbolPageCount
        get() = symbolPages.size
    private val symbolPages = listOf(

        listOf(
            "1234567890",
            "@#\$_&-+()/",
            "*\"\':;!?",
        ),

        listOf(
            "~`|•√π÷×§∆",
            "£¢€¥^°={}\\",
            "%©®™✓[]",
        )
    )

    private val altMap = mapOf(
        'a' to listOf("á", "à", "ä", "â", "ã"),
        'e' to listOf("é", "è", "ë", "ê"),
        'i' to listOf("í", "ì", "ï", "î"),
        'o' to listOf("ó", "ò", "ö", "ô", "õ"),
        'u' to listOf("ú", "ù", "ü", "û")
    )

    private fun getAltChars(c: Char): List<String> {
        return altMap[c.lowercaseChar()] ?: emptyList()
    }

    fun create(config: LayoutConfig, language: KeyboardLanguage): List<List<KeySpec>> {

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
        rows += createBottomRow(config, language)

        return rows
    }

    private fun createCharRow(
        chars: String, config: LayoutConfig, lastRow: Boolean
    ): List<KeySpec> {
        val rowKeys = mutableListOf<KeySpec>()
        if (lastRow && config.hasShift && config.mode == KeyboardMode.LETTERS) {
            rowKeys.add(
                KeySpec(
                    type = KeyType.SHIFT, iconDrawable = IconImage(
                        "shift", when (config.shiftState) {
                            ShiftState.CAPS_LOCK -> R.drawable.img_shift_lock
                            ShiftState.ON -> R.drawable.img_shift_once
                            else -> R.drawable.img_shift_unused
                        }, null
                    ), weight = 1.5f
                ),
            )
        }
        if (lastRow && config.mode == KeyboardMode.SYMBOLS) {
            rowKeys.add(
                KeySpec(
                    type = KeyType.NEXT_SYMBOL,
                    label = "${config.pageIndex + 1}/${symbolPages.size}",
                    weight = 1.5f
                ),
            )
        }
        rowKeys.addAll(chars.map { c ->

            val finalChar = when (config.shiftState) {
                ShiftState.OFF -> c.lowercaseChar()
                ShiftState.ON, ShiftState.CAPS_LOCK -> c.uppercaseChar()
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
                    isRepeatable = true,
                    weight = 1.5f
                )
            )

        }
        return rowKeys
    }

    private fun createBottomRow(
        config: LayoutConfig, language: KeyboardLanguage
    ): List<KeySpec> {
        //            KeySpec(type = KeyType.CANCEL, icon = Icons.Default.ArrowDropDown, weight = 1.5f),
//            KeySpec(type = KeyType.SETTINGS, icon = Icons.Default.Settings),
//            KeySpec(type = KeyType.LANGUAGE, icon = Icons.Default.Language),
        return if (config.mode == KeyboardMode.LETTERS) {
            listOf(
                KeySpec(
                    type = KeyType.SYMBOL,
                    label = "?123",
                    weight = 1.5f
                ),
                KeySpec(
                    type = KeyType.INPUT, label = if (language == KeyboardLanguage.CHINESE) {
                        "，"
                    } else {
                        ","
                    }
                ),
                KeySpec(
                    label = language.displayName,
                    32,
                    type = KeyType.SPACE,
                    icon = Icons.Default.SpaceBar,
                    weight = 5f
                ),
                KeySpec(
                    type = KeyType.INPUT, label = if (language == KeyboardLanguage.CHINESE) {
                        "。"
                    } else {
                        "."
                    }
                ),
                KeySpec(
                    type = KeyType.ENTER,
                    icon = Icons.Default.KeyboardReturn,
                    iconDrawable = IconImage(
                        "FIFA soccer", R.drawable.ic_soccer, BuildConfig.Fifa2026ExpireDate
                    ),
                    weight = 1.5f
                )
            )
        } else {
            listOf(
                KeySpec(
                    type = KeyType.SYMBOL,
                    label = "ABC",
                    weight = 1.5f
                ),
                KeySpec(
                    type = KeyType.INPUT,
                    label = "<",
                    altChars = listOf("<", "≤", "《", "〈", "【")
                ),
                KeySpec(
                    label = language.displayName,
                    32,
                    type = KeyType.SPACE,
                    icon = Icons.Default.SpaceBar,
                    weight = 5f
                ),
                KeySpec(
                    type = KeyType.INPUT,
                    label = ">",
                    altChars = listOf(">", "≥", "》", "〉", "】")
                ),
                KeySpec(
                    type = KeyType.ENTER,
                    icon = Icons.Default.KeyboardReturn,
                    iconDrawable = IconImage(
                        "FIFA soccer", R.drawable.ic_soccer, BuildConfig.Fifa2026ExpireDate
                    ),
                    weight = 1.5f
                )
            )
        }
    }
}