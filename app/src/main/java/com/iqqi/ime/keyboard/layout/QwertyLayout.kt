package com.iqqi.ime.keyboard.layout

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
        KeySpec(type = KeyType.SHIFT),
        KeySpec("z", 122),
        KeySpec("x", 120),
        KeySpec("c", 99),
        KeySpec("v", 118),
        KeySpec("b", 98),
        KeySpec("n", 110),
        KeySpec("m", 109),
        KeySpec(",", 44),
        KeySpec(type = KeyType.DELETE, isRepeatable = true)
    ),
    listOf(
        KeySpec(type = KeyType.CANCEL),
        KeySpec(type = KeyType.SETTINGS),
        KeySpec(type = KeyType.LANGUAGE),
        KeySpec(" ", 32, KeyType.SPACE, weight = 5f),
        KeySpec(type = KeyType.SYMBOL),
        KeySpec(type = KeyType.ENTER)
    )
)