package com.iqqi.dictionary

import com.iqqi.dictionary.adapter.KikaEngineAdapter

class KikaDictionary(
    private val engineId: Int
) : Dictionary {

    private val engine = KikaEngineAdapter()

    private val pageSize = 20

    override fun init(predictDict: String, userDataDict: String?): Int {

        val dictName = getDictName(engineId)

        val pred = "$predictDict$dictName"
        val user = "$userDataDict${dictName.replace(".so", ".u.so")}"

        return engine.init(engineId, pred, user)
    }

    override fun query(code: String): List<String> {
        return engine.candidates(
            engineId,
            code,
            0,
            pageSize
        )
    }

    override fun predict(previous: String): List<String> {
        return engine.predict(
            engineId,
            previous,
            0,
            pageSize
        )
    }

    override fun composing(compose: String): String {
        return engine.composing(
            engineId,
            compose
        )
    }

    private fun getDictName(engineId: Int): String {
        return dictMap[engineId] ?: T9DictFileName.ENGLISH
    }

    companion object {
        val dictMap: Map<Int, String> = mapOf(
            0 to T9DictFileName.ENGLISH,       // IQQI_IME_None
            1 to T9DictFileName.ENGLISH,       // IQQI_IME_English_US
            2 to T9DictFileName.FRENCH,        // IQQI_IME_French_CA
            3 to T9DictFileName.PORTUGUESE_BR, // IQQI_IME_Portuguese_BR
            4 to T9DictFileName.SPANISH_US,    // IQQI_IME_Spanish_US
            5 to T9DictFileName.ENGLISH,       // IQQI_IME_Assamese (沒有對應檔案，暫用英文)
            6 to T9DictFileName.BULGARIAN,
            7 to T9DictFileName.ENGLISH,       // IQQI_IME_Bodo
            8 to T9DictFileName.ENGLISH,       // IQQI_IME_Dogri
            9 to T9DictFileName.ENGLISH,       // IQQI_IME_Gujarati
            10 to T9DictFileName.ENGLISH,      // IQQI_IME_Hindi
            11 to T9DictFileName.ENGLISH,      // IQQI_IME_Kannada
            12 to T9DictFileName.ENGLISH,      // IQQI_IME_Kashmiri
            13 to T9DictFileName.ENGLISH,      // IQQI_IME_Konkani
            14 to T9DictFileName.ENGLISH,      // IQQI_IME_Malayalam
            15 to T9DictFileName.ENGLISH,      // IQQI_IME_Manipuri
            16 to T9DictFileName.ENGLISH,      // IQQI_IME_Marathi
            17 to T9DictFileName.ENGLISH,      // IQQI_IME_Nepali
            18 to T9DictFileName.ENGLISH,      // IQQI_IME_Oriya
            19 to T9DictFileName.ENGLISH,      // IQQI_IME_Punjabi
            20 to T9DictFileName.ENGLISH,      // IQQI_IME_Sanskrit
            21 to T9DictFileName.ENGLISH,      // IQQI_IME_Santhali
            22 to T9DictFileName.ENGLISH,      // IQQI_IME_Sindhi
            23 to T9DictFileName.ENGLISH,      // IQQI_IME_Tamil
            24 to T9DictFileName.ENGLISH,      // IQQI_IME_Telugu
            25 to T9DictFileName.ARABIC,       // IQQI_IME_Urdu
            26 to T9DictFileName.ARABIC,       // IQQI_IME_Afrikaans
            27 to T9DictFileName.ARABIC,       // IQQI_IME_Arabic
            28 to T9DictFileName.CHINESE_CN,
            29 to T9DictFileName.DUTCH,
            30 to T9DictFileName.ENGLISH,      // English_GB
            31 to T9DictFileName.FRENCH,
            32 to T9DictFileName.GERMAN,
            33 to T9DictFileName.HUNGARIAN,
            34 to T9DictFileName.INDONESIAN,
            35 to T9DictFileName.ITALIAN,
            36 to T9DictFileName.MALAY,
            37 to T9DictFileName.PERSIAN,
            38 to T9DictFileName.POLISH,
            39 to T9DictFileName.PORTUGUESE_PT,
            40 to T9DictFileName.ROMANIAN,
            41 to T9DictFileName.RUSSIAN,
            42 to T9DictFileName.SPANISH_ES,
            43 to T9DictFileName.ENGLISH,      // SWAHILI
            44 to T9DictFileName.THAI,
            45 to T9DictFileName.TURKISH,
            46 to T9DictFileName.ENGLISH,      // VIETNAMESE
            47 to T9DictFileName.ENGLISH,      // Zulu
            48 to T9DictFileName.ENGLISH,      // 你可以自己補對應
            49 to T9DictFileName.BULGARIAN,
            50 to T9DictFileName.CROATIAN,
            51 to T9DictFileName.CZECH,
            52 to T9DictFileName.FINNISH,
            53 to T9DictFileName.GREEK,
            54 to T9DictFileName.KAZAKH,
            55 to T9DictFileName.ENGLISH,      // Khmer
            56 to T9DictFileName.ENGLISH,      // Macedonian
            57 to T9DictFileName.SERBIAN,
            58 to T9DictFileName.ENGLISH,      // Sinhala
            59 to T9DictFileName.SLOVAK,
            60 to T9DictFileName.ENGLISH,      // Slovenian
            61 to T9DictFileName.SWEDISH,
            62 to T9DictFileName.ENGLISH,      // Tagalog
            63 to T9DictFileName.UKRAINIAN,
            64 to T9DictFileName.ENGLISH,      // Xhosa
            65 to T9DictFileName.ENGLISH,      // Albanian
            66 to T9DictFileName.ENGLISH,      // Armenian
            67 to T9DictFileName.ENGLISH,      // Azerbaijani
            68 to T9DictFileName.ENGLISH,      // Belarusian
            69 to T9DictFileName.ENGLISH,      // Bosnian
            70 to T9DictFileName.CHINESE_HK,
            71 to T9DictFileName.CHINESE_TW,
            72 to T9DictFileName.DANISH,
            73 to T9DictFileName.ESTONIAN,
            74 to T9DictFileName.ENGLISH,      // Georgian
            75 to T9DictFileName.HEBREW,
            76 to T9DictFileName.ENGLISH,      // Icelandic
            77 to T9DictFileName.ENGLISH,      // Lao
            78 to T9DictFileName.LATVIAN,
            79 to T9DictFileName.LITHUANIAN,
            80 to T9DictFileName.NORWEGIAN,
            81 to T9DictFileName.ENGLISH,      // Uzbek_Cyrillic
            82 to T9DictFileName.ENGLISH,      // Basque
            83 to T9DictFileName.ENGLISH,      // Galician
            84 to T9DictFileName.ENGLISH,      // Malagasy
            85 to T9DictFileName.ENGLISH,      // Yiddish
            86 to T9DictFileName.KOREAN,
            87 to T9DictFileName.ENGLISH,      // Catalan
            88 to T9DictFileName.ENGLISH,      // Japanese_Kana
            89 to T9DictFileName.ENGLISH,      // Japanese_Roma
            90 to T9DictFileName.ENGLISH,      // Changjie
            91 to T9DictFileName.ENGLISH,      // Quick_CJ
            92 to T9DictFileName.ENGLISH,      // Farsi
            93 to T9DictFileName.ENGLISH,      // Maithili
            94 to T9DictFileName.ENGLISH,      // Burmese_Z
            95 to T9DictFileName.ENGLISH,      // Burmese_U
            96 to T9DictFileName.ENGLISH,      // Tibetan
            97 to T9DictFileName.ENGLISH,      // Uyghur_tili
            98 to T9DictFileName.ENGLISH,      // Jawa
            99 to T9DictFileName.ENGLISH,      // Maori
            100 to T9DictFileName.ENGLISH,     // Mongolian
            101 to T9DictFileName.ENGLISH      // Amharic
        )
    }
}

object T9DictFileName {
    const val ARABIC: String = "/libArabic.so"
    const val BULGARIAN: String = "/libBulgarian.so"
    const val CZECH: String = "/libCzech.so"
    const val CROATIAN: String = "/libCroatian.so"
    const val DANISH: String = "/libDanish.so"
    const val DUTCH: String = "/libDutch.so"
    const val ENGLISH: String = "/libEnglish_US.so"
    const val ESTONIAN: String = "/libEstonian.so"
    const val FINNISH: String = "/libFinnish.so"
    const val FRENCH: String = "/libFrench_FR.so"
    const val GERMAN: String = "/libGerman.so"
    const val GREEK: String = "/libGreek.so"
    const val HEBREW: String = "/libHebrew.so"
    const val HUNGARIAN: String = "/libHungarian.so"
    const val INDONESIAN: String = "/libIndonesian.so"
    const val ITALIAN: String = "/libItalian.so"
    const val KAZAKH: String = "/libKazakh.so"
    const val KOREAN: String = "/libKorean.so"
    const val LATVIAN: String = "/libLatvian.so"
    const val LITHUANIAN: String = "/libLithuanian.so"
    const val MALAY: String = "/libMalay.so"
    const val NORWEGIAN: String = "/libNorwegian.so"
    const val PERSIAN: String = "/libPersian.so"
    const val POLISH: String = "/libPolish.so"
    const val PORTUGUESE_BR: String = "/libPortuguese_BR.so"
    const val PORTUGUESE_PT: String = "/libPortuguese_PT.so"
    const val ROMANIAN: String = "/libRomanian.so"
    const val RUSSIAN: String = "/libRussian.so"
    const val SERBIAN: String = "/libSerbian.so"
    const val SLOVAK: String = "/libSlovak.so"
    const val SPANISH_ES: String = "/libSpanish_ES.so"
    const val SPANISH_US: String = "/libSpanish_US.so"
    const val SWEDISH: String = "/libSwedish.so"
    const val THAI: String = "/libThai.so"
    const val TURKISH: String = "/libTurkish.so"
    const val UKRAINIAN: String = "/libUkrainian.so"
    const val CHINESE_CN: String = "/libChinese_CN.so"
    const val CHINESE_HK: String = "/libChinese_HK.so"
    const val CHINESE_TW: String = "/libChinese_TW.so"
}