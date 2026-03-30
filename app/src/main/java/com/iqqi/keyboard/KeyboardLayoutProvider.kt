package com.iqqi.keyboard

import android.text.InputType
import android.view.inputmethod.EditorInfo
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

    private val letterRowsMap = mapOf(
        KeyboardLanguage.ENGLISH to listOf(
            "qwertyuiop", "asdfghjkl", "zxcvbnm"
        ), KeyboardLanguage.CHINESE to listOf( // 注音 or 倉頡你之後可換
            "qwertyuiop", "asdfghjkl", "zxcvbnm"
        )
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

    private fun getLetterRows(language: KeyboardLanguage): List<String> {
        return letterRowsMap[language] ?: letterRowsMap[KeyboardLanguage.ENGLISH]!!
    }

    //=======================================

    fun create(
        config: LayoutConfig, language: KeyboardLanguage, inputType: Int
    ): List<List<KeySpec>> {

        val inputClass = inputType and InputType.TYPE_MASK_CLASS

        return when (config.mode) {
            KeyboardMode.LETTERS -> {
                when (inputClass) {
                    InputType.TYPE_CLASS_DATETIME, InputType.TYPE_CLASS_PHONE, InputType.TYPE_CLASS_NUMBER -> {
                        createNumberLayout(config, inputType = inputType)
                    }

                    else -> createLetterLayout(config, language, inputType)
                }
            }

            KeyboardMode.SYMBOLS -> createSymbolLayout(config, language, inputType)
        }
    }

    //=======================================

    private fun createLetterLayout(
        config: LayoutConfig, language: KeyboardLanguage, inputType: Int
    ): List<List<KeySpec>> {
        val rows = mutableListOf<List<KeySpec>>()

        // 1️⃣ Number row
        if (config.showNumberRow) {
            rows += createCharRow(
                chars = numberRow, config = config, lastRow = false, inputType = inputType
            )
        }

        val letterRowsBase = getLetterRows(language)
        // 2️⃣ Main rows
        letterRowsBase.forEach { row ->
            rows += createCharRow(
                chars = row,
                config = config,
                lastRow = row == letterRowsBase.last(),
                inputType = inputType
            )
        }

        // 3️⃣ 功能列
        rows += listOf(
            KeySpec(
                type = KeyType.SYMBOL, label = "?123", weight = 1.5f
            ), generateSpaceSideKey(
                mode = KeyboardMode.LETTERS,
                isLeft = true,
                language = language,
                inputType = inputType
            ), KeySpec(
                label = language.displayName,
                32,
                type = KeyType.SPACE,
                icon = Icons.Default.SpaceBar,
                weight = 5f
            ), generateSpaceSideKey(
                mode = KeyboardMode.LETTERS,
                isLeft = false,
                language = language,
                inputType = inputType
            ), KeySpec(
                type = KeyType.ENTER, icon = Icons.Default.KeyboardReturn, iconDrawable = IconImage(
                    "FIFA soccer", R.drawable.ic_soccer, BuildConfig.Fifa2026ExpireDate
                ), weight = 1.5f
            )
        )

        return rows
    }

    private fun createSymbolLayout(
        config: LayoutConfig, language: KeyboardLanguage, inputType: Int
    ): List<List<KeySpec>> {
        val rows = mutableListOf<List<KeySpec>>()

        val page = config.pageIndex % symbolPages.size
        val targetSymbol = symbolPages[page]
        targetSymbol.forEach { row ->
            rows += createCharRow(
                chars = row,
                config = config,
                lastRow = row == targetSymbol.last(),
                inputType = inputType
            )
        }

        // 3️⃣ 功能列
        rows += listOf(
            KeySpec(
                type = KeyType.SYMBOL, label = "ABC", weight = 1.5f
            ), generateSpaceSideKey(
                mode = KeyboardMode.SYMBOLS,
                isLeft = true,
                language = language,
                inputType = inputType
            ), KeySpec(
                label = language.displayName,
                code = 32,
                type = KeyType.SPACE,
                icon = Icons.Default.SpaceBar,
                weight = 5f
            ), generateSpaceSideKey(
                mode = KeyboardMode.SYMBOLS,
                isLeft = false,
                language = language,
                inputType = inputType
            ), KeySpec(
                type = KeyType.ENTER, icon = Icons.Default.KeyboardReturn, iconDrawable = IconImage(
                    "FIFA soccer", R.drawable.ic_soccer, BuildConfig.Fifa2026ExpireDate
                ), weight = 1.5f
            )
        )
        return rows
    }

    private fun createNumberLayout(config: LayoutConfig, inputType: Int): List<List<KeySpec>> {
        return listOf(
            createCharRow("123-", config, false, inputType = inputType),
            createCharRow("456", config, false, inputType = inputType) + KeySpec(
                code = 32, type = KeyType.SPACE, icon = Icons.Default.SpaceBar, weight = 1f
            ),
            createCharRow("789", config, false, inputType = inputType) +

                    KeySpec(
                        type = KeyType.DELETE,
                        icon = Icons.Default.Backspace,
                        isRepeatable = true,
                        weight = 1f
                    ),
            createCharRow(",0.", config, false, inputType = inputType) + KeySpec(
                type = KeyType.ENTER, icon = Icons.Default.KeyboardReturn, iconDrawable = IconImage(
                    "FIFA soccer", R.drawable.ic_soccer, BuildConfig.Fifa2026ExpireDate
                ), weight = 1f
            )
        )
    }

    //=======================================

    private fun createCharRow(
        chars: String, config: LayoutConfig, lastRow: Boolean, inputType: Int
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

            val baseKey = KeySpec(
                label = finalChar.toString(),
                code = finalChar.code,
                altChars = getAltChars(finalChar)
            )

            transformKey(baseKey, inputType)
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

    private fun transformKey(key: KeySpec, inputType: Int): KeySpec {

        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION

        return when (variation) {

            // 📧 EMAIL
            EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                when (key.label) {
                    "." -> key.copy(
                        altChars = listOf(".gov", ".edu", ".org", ".com", ".net")
                    )

                    "@" -> key.copy(
                        altChars = listOf("@gmail", "@yahoo", "@outlook", "@icloud")
                    )

                    else -> key
                }
            }

            // 🌐 URL
            EditorInfo.TYPE_TEXT_VARIATION_URI -> {
                when (key.label) {
                    "." -> key.copy(
                        altChars = listOf(".gov", ".edu", ".org", ".com", ".net")
                    )

                    "/" -> key.copy(
                        altChars = listOf("www.", "https://", "http://")
                    )

                    else -> key
                }
            }

            // 🔒 PASSWORD
            EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> {
                key.copy(
                    altChars = emptyList()
                )
            }

            else -> key
        }
    }

    private fun generateSpaceSideKey(
        mode: KeyboardMode, isLeft: Boolean, language: KeyboardLanguage, inputType: Int
    ): KeySpec {

        val variation = inputType and EditorInfo.TYPE_MASK_VARIATION

        val rawKey = when (mode) {

            KeyboardMode.LETTERS -> {

                when (variation) {

                    // 📧 EMAIL → 左右給 , .
                    EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                        if (isLeft) {
                            KeySpec(type = KeyType.INPUT, label = "@")
                        } else {
                            KeySpec(type = KeyType.INPUT, label = ".")
                        }
                    }

                    // 🌐 URL → 強化 dot / slash
                    EditorInfo.TYPE_TEXT_VARIATION_URI -> {
                        if (isLeft) {
                            KeySpec(type = KeyType.INPUT, label = "/")
                        } else {
                            KeySpec(type = KeyType.INPUT, label = ".")
                        }
                    }

                    // 🔒 PASSWORD → 不要干擾
                    EditorInfo.TYPE_TEXT_VARIATION_PASSWORD, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD -> {
                        if (isLeft) {
                            KeySpec(type = KeyType.INPUT, label = ",")
                        } else {
                            KeySpec(type = KeyType.INPUT, label = ".")
                        }
                    }

                    // 🧠 一般文字輸入（重點）
                    else -> {
                        if (isLeft) {
                            KeySpec(
                                type = KeyType.INPUT,
                                label = if (language == KeyboardLanguage.CHINESE) "，" else ","
                            )
                        } else {
                            KeySpec(
                                type = KeyType.INPUT,
                                label = if (language == KeyboardLanguage.CHINESE) "。" else "."
                            )
                        }
                    }
                }
            }

            // 🔣 SYMBOL 模式
            KeyboardMode.SYMBOLS -> {
                if (isLeft) {
                    KeySpec(
                        type = KeyType.INPUT,
                        label = "<",
                        altChars = listOf("<", "≤", "《", "〈", "【")
                    )
                } else {
                    KeySpec(
                        type = KeyType.INPUT,
                        label = ">",
                        altChars = listOf(">", "≥", "》", "〉", "】")
                    )
                }
            }
        }

        return transformKey(rawKey, inputType)
    }
    //=======================================
}