package com.iqqi.ime.keyboard.layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpaceBar
import com.iqqi.ime.keyboard.model.KeySpec
import com.iqqi.ime.keyboard.model.KeyType

val englishLayout = listOf(
    listOf(
        KeySpec("q", 113),
        KeySpec("w", 119),
        KeySpec("e", 101),
        KeySpec("r", 114),
        KeySpec("t", 116),
        KeySpec("y", 121),
        KeySpec("u", 117),
        KeySpec("i", 105),
        KeySpec("o", 111),
        KeySpec("p", 112),
    ),
    listOf(
        KeySpec("a", 97),
        KeySpec("s", 115),
        KeySpec("d", 100),
        KeySpec("f", 102),
        KeySpec("g", 103),
        KeySpec("h", 104),
        KeySpec("j", 106),
        KeySpec("k", 107),
        KeySpec("l", 108),
        KeySpec(".", 46)
    ),
    listOf(
        KeySpec(type = KeyType.SHIFT, icon = Icons.Default.KeyboardArrowUp),
        KeySpec("z", 122),
        KeySpec("x", 120),
        KeySpec("c", 99),
        KeySpec("v", 118),
        KeySpec("b", 98),
        KeySpec("n", 110),
        KeySpec("m", 109),
        KeySpec(",", 44),
        KeySpec(type = KeyType.DELETE, icon = Icons.Default.Backspace, isRepeatable = true)
    ),
    listOf(
        KeySpec(type = KeyType.CANCEL, icon = Icons.Default.ArrowDropDown),
        KeySpec(type = KeyType.SETTINGS, icon = Icons.Default.Settings),
        KeySpec(type = KeyType.LANGUAGE, icon = Icons.Default.Language),
        KeySpec(" ", 32, type = KeyType.SPACE, icon = Icons.Default.SpaceBar, weight = 5f),
        KeySpec(type = KeyType.SYMBOL, icon = Icons.Default.DashboardCustomize),
        KeySpec(type = KeyType.ENTER, icon = Icons.Default.KeyboardReturn)
    )
)