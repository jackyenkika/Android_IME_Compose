package com.iqqi.ime

import android.inputmethodservice.InputMethodService
import android.text.InputType
import com.iqqi.core.EngineOutput
import com.iqqi.ime.util.DeleteObj
import com.iqqi.ime.util.LogObj
import com.iqqi.ime.util.ToolObj.expireTimestamp
import com.iqqi.keyboard.model.KeyboardLanguage

data class CommitContext(
    val text: String, val beforeCursor: String, val language: KeyboardLanguage
)

class SmartCommitProcessor {
    private val autoSpacePunctuation = setOf(",", ".", "!", "?", ":", ";")
    private var lastSpaceTimestamp: Long = 0L
    private val DOUBLE_SPACE_INTERVAL = 700L

    fun process(ctx: CommitContext, onDoubleSpaceTriggered: () -> Unit): String {
        var text = ctx.text
        val now = System.currentTimeMillis()
        if (ctx.language != KeyboardLanguage.ENGLISH) return text

        when {
            text == " " -> handleSpace(
                ctx.beforeCursor, now, onDoubleSpaceTriggered
            )?.let { return it }

            text.endsWith(" ") -> lastSpaceTimestamp = now
            else -> lastSpaceTimestamp = 0L
        }

        if (text in autoSpacePunctuation && !ctx.beforeCursor.endsWith(" ")) {
            text += " "
        }
        return text
    }

    private fun handleSpace(
        before: String, now: Long, onDoubleSpaceTriggered: () -> Unit
    ): String? {
        val len = before.length
        val last = before.getOrNull(len - 1)
        val secondLast = before.getOrNull(len - 2)
        val isQuick = (now - lastSpaceTimestamp) < DOUBLE_SPACE_INTERVAL

        if (last == ' ' && secondLast != null && secondLast != ' ' && secondLast.toString() !in autoSpacePunctuation && isQuick) {
            lastSpaceTimestamp = 0L
            onDoubleSpaceTriggered()
            return ". "
        }
        lastSpaceTimestamp = now
        return null
    }
}

class CaseProcessor {

    fun process(
        text: String,
        beforeCursor: String,
        composing: String,
        inputType: Int,
        language: KeyboardLanguage
    ): String {
        if (language != KeyboardLanguage.ENGLISH || text.isEmpty()) return text
        if (isPassword(inputType)) return text
        if (isEmail(inputType)) return text.lowercase()

        val capsMode = getCapsMode(inputType)
        val boundaryText = beforeCursor + composing

        return when {
            text.length == 1 -> handleSingleChar(text, boundaryText, capsMode)
            capsMode == CapsMode.CHARACTERS -> text.uppercase()
            capsMode == CapsMode.WORDS -> if (isWordBoundary(boundaryText)) text.capitalize() else text
            capsMode == CapsMode.SENTENCES -> if (isSentenceBoundary(boundaryText)) text.capitalize() else text
            else -> text
        }
    }

    private fun isPassword(inputType: Int) =
        inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD != 0

    private fun isEmail(inputType: Int) =
        inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS != 0

    private fun getCapsMode(inputType: Int) = when {
        inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS != 0 -> CapsMode.CHARACTERS
        inputType and InputType.TYPE_TEXT_FLAG_CAP_WORDS != 0 -> CapsMode.WORDS
        inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0 -> CapsMode.SENTENCES
        else -> CapsMode.NONE
    }

    private fun isSentenceBoundary(before: String): Boolean {
        val trimmed = before.trimEnd()
        if (trimmed.isEmpty()) return true
        return trimmed.last() in listOf('.', '!', '?', '\n')
    }

    private fun isWordBoundary(before: String): Boolean =
        before.isEmpty() || before.last().isWhitespace()

    private fun handleSingleChar(text: String, before: String, mode: CapsMode): String =
        when (mode) {
            CapsMode.CHARACTERS -> text.uppercase()
            CapsMode.WORDS, CapsMode.SENTENCES -> if (before.isBlank() || before.lastOrNull()
                    ?.isWhitespace() == true
            ) text.uppercase() else text

            else -> text
        }

    private fun String.capitalize(): String = replaceFirstChar { it.uppercase() }

    private enum class CapsMode { NONE, SENTENCES, WORDS, CHARACTERS }
}

class IMERenderer(private val ims: InputMethodService) {

    private val commitProcessor = SmartCommitProcessor()
    private val caseProcessor = CaseProcessor()
    private var isAppExpired = false

    init {
        BuildConfig.AppExpireDate.expireTimestamp()
            ?.let { isAppExpired = it < System.currentTimeMillis() }
    }

    fun render(output: EngineOutput, language: KeyboardLanguage) {
        val ic = ims.currentInputConnection ?: return
        val inputType = IMEStore.keyboardState.value.inputType
        val before = ic.getTextBeforeCursor(10, 0)?.toString() ?: ""
        val composing = output.composingText ?: ""

        if (output.deleteBeforeCursor) {
            ic.finishComposingText()
            DeleteObj.delete(ic)
        }

        output.commitText?.let { raw ->
            val caseAdjusted = caseProcessor.process(raw, before, composing, inputType, language)
            val finalText = commitProcessor.process(
                CommitContext(
                    caseAdjusted, before, language
                )
            ) { ic.deleteSurroundingText(1, 0) }
            ic.commitText(finalText, 1)
            IMEStore.commitEvents.tryEmit(finalText)
        }

        LogObj.trace("render composing: $composing")
        if (composing.isNotEmpty()) ic.setComposingText(composing, 1) else ic.finishComposingText()

        if (output.candidates.isEmpty()) {
            IMEStore.clearCandidate()
            return
        }

        if (!isAppExpired) {
            LogObj.trace("inputType = $inputType , raw candidates1 : ${output.candidates}, selectedIndex = ${output.selectedIndex}")
            // ✅ 使用 inputType + CapsMode 轉換候選字
            val adjusted = output.candidates.map {
                caseProcessor.process(it, before, composing, inputType, language)
            }
            LogObj.trace("render candidates2 : $adjusted, selectedIndex = ${output.selectedIndex}")
            IMEStore.updateCandidate(adjusted, output.selectedIndex)
        }
    }
}