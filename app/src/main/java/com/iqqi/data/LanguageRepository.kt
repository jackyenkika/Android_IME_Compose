package com.iqqi.data

import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import com.iqqi.keyboard.model.ImeLanguage
import com.iqqi.keyboard.model.KeyboardLanguage

class LanguageRepository(
    private val imm: InputMethodManager,
    private val packageName: String
) {

    fun getAvailableLanguages(): List<ImeLanguage> {

        val imi = imm.enabledInputMethodList
            .firstOrNull { it.packageName == packageName }
            ?: return emptyList()

        val subtypes = imm.inputMethodList
            .filter { it.packageName == packageName }
            .flatMap { imiInfo ->
                (0 until imiInfo.subtypeCount).map { i ->
                    imiInfo.getSubtypeAt(i)
                }
            }

        val enabledSubtypes =
            imm.getEnabledInputMethodSubtypeList(imi, true)

        return subtypes.map { subtype ->
            subtype.toImeLanguage(enabledSubtypes)
        }
    }

    // 👉 抽成 extension function（乾淨很多）
    private fun InputMethodSubtype.toImeLanguage(
        enabledList: List<InputMethodSubtype>
    ): ImeLanguage {

        val locale = this.locale

        val language = when {
            locale.startsWith("zh") -> KeyboardLanguage.CHINESE
            else -> KeyboardLanguage.ENGLISH
        }

        return ImeLanguage(
            name = language,
            locale = locale,
            subtype = this,
            enabled = enabledList.any { it.locale == locale }
        )
    }
}