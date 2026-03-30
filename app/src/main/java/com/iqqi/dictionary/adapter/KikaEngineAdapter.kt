package com.iqqi.dictionary.adapter

import com.iqqi.ime.util.LogObj
import kika.qwt9.inputmethod.Resource.qwt9ini
import java.util.Locale

class KikaEngineAdapter {

    fun init(engineId: Int, predPath: String, userPath: String): Int {
        qwt9ini.setQwerty()
        return qwt9ini.initial(getImeId(engineId), predPath, userPath)
    }

    fun candidates(
        engineId: Int,
        code: String,
        begin: Int,
        number: Int
    ): List<String> {
        val arr = arrayOfNulls<String>(number)
        val count = qwt9ini.GetCandidates(
            getImeId(engineId),
            code.lowercase(Locale.US),
            true,
            0,
            begin,
            number,
            arr,
            null
        )

        val rawList = arr.take(count).filterNotNull().toMutableList()

        // --- 針對英語 "i" 的核心邏輯修正 ---
        // 當使用者輸入單個 "i" 時，我們希望候選字清單中出現 "I"，且排在第一位
        if (code.lowercase(Locale.US) == "i") {
            // 1. 檢查清單中是否有 "i" 或 "I"
            val iIndex = rawList.indexOfFirst { it.equals("i", ignoreCase = true) }

            if (iIndex != -1) {
                // 移除舊的，並在首位插入正確的大寫 "I"
                rawList.removeAt(iIndex)
                rawList.add(0, "I")
            } else {
                // 如果引擎居然沒給 "i"，我們手動補在第一位
                rawList.add(0, "I")
            }
        }

        LogObj.debug("candidates arr: $rawList")
        return rawList
    }

    fun predict(
        engineId: Int,
        previous: String,
        begin: Int,
        number: Int
    ): List<String> {
        val arr = arrayOfNulls<String>(number)
        val count = qwt9ini.GetNextWordCandidates(
            getImeId(engineId),
            previous,
            begin,
            number,
            arr
        )

        LogObj.debug("predict arr: ${arr.contentToString()}")
        return arr.take(count).filterNotNull()
    }

    fun composing(engineId: Int, code: String): String {
        val imeId = getImeId(engineId)
        if (imeId == qwt9ini.IQQI_IME_ID.IQQI_IME_Korean) {
            return qwt9ini.IQQI_GetComposingText(imeId, code, 0, 0)
        }
        return code
    }

    private fun getImeId(engineId: Int): qwt9ini.IQQI_IME_ID {
        return qwt9ini.IQQI_IME_ID.entries[engineId]
    }
}