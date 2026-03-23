package com.iqqi.settings.ui.theme

import androidx.annotation.FontRes

enum class FontType(val label: String, @FontRes val res: Int? = null) {
    SYSTEM("System Default"),
    SANS("Sans Serif"),
    SERIF("Serif"),
    MONO("Monospace"),

    //CUSTOM_FONT_TYPE
//    HFDimenPixel_CBDT("HFDimenPixel-CBDT", R.font.hf_dimen_pixel_cbdt),
}