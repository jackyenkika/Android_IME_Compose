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

        LogObj.debug("candidates arr: ${arr.contentToString()}")

        return arr.take(count).filterNotNull()
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