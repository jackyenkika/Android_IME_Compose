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
    enum class CapsMode { NONE, SENTENCES, WORDS, CHARACTERS }

    // 根據 InputType 判斷當前的大寫模式
    fun getCapsMode(inputType: Int): CapsMode {
        return when {
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0 -> CapsMode.CHARACTERS
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0 -> CapsMode.WORDS
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0 -> CapsMode.SENTENCES
            else -> CapsMode.NONE
        }
    }

    // 判定當前游標位置是否處於「需要大寫」的邊界
    fun isAtBoundary(before: String, mode: CapsMode): Boolean {
        // --- 核心修正：如果是 NONE，絕對不觸發大寫 ---
        if (mode == CapsMode.NONE) return false

        if (mode == CapsMode.CHARACTERS) return true

        // 如果前面是空的，通常視為 Session 的開始，判定為邊界
        if (before.isEmpty()) return true

        val trimmed = before.trimEnd()
        // 如果全是空格，對 SENTENCES 來說是句首，對 WORDS 來說是詞首
        if (trimmed.isEmpty()) return true

        return when (mode) {
            CapsMode.SENTENCES -> {
                val lastChar = trimmed.last()
                lastChar == '.' || lastChar == '!' || lastChar == '?' || lastChar == '\n'
            }

            CapsMode.WORDS -> before.last().isWhitespace()
            else -> false
        }
    }

    // 執行轉換：首字母大寫或全大寫
    fun applyCase(text: String, shouldCap: Boolean, mode: CapsMode): String {
        if (text.isEmpty()) return text

        // --- 核心修正：如果模式是 NONE，直接原樣回傳 ---
        if (mode == CapsMode.NONE) return text

        if (mode == CapsMode.CHARACTERS) return text.uppercase()

        return if (shouldCap) {
            text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        } else {
            text
        }
    }
}

class IMERenderer(private val ims: InputMethodService) {

    private val commitProcessor = SmartCommitProcessor()
    private val caseProcessor = CaseProcessor()
    private var isAppExpired = false
    private var isSessionStartingAtBoundary = false   // 鎖定當前輸入 Session 是否從大寫邊界開始

    init {
        BuildConfig.AppExpireDate.expireTimestamp()
            ?.let { isAppExpired = it < System.currentTimeMillis() }
    }

    fun render(output: EngineOutput, language: KeyboardLanguage) {
        val ic = ims.currentInputConnection ?: return
        val keyboardState = IMEStore.keyboardState.value
        val inputType = keyboardState.inputType
        val composing = output.composingText ?: ""
        val capsMode = caseProcessor.getCapsMode(inputType)

        // --- 第一步：處理刪除 ---
        if (output.deleteBeforeCursor) {
            ic.finishComposingText()
            DeleteObj.delete(ic)
        }

        // --- 第二步：處理 Commit ---
        // 這樣可以確保文字先上屏，ic.getTextBeforeCursor 才能抓到正確的後續環境
        output.commitText?.let { raw ->
            // 這裡必須使用「進入這一幀之前」的 isSessionStartingAtBoundary
            val caseAdjusted = if (raw.isNotEmpty() && raw != " " && raw != "\n") {
                caseProcessor.applyCase(raw, isSessionStartingAtBoundary, capsMode)
            } else {
                raw
            }

            val currentBefore = ic.getTextBeforeCursor(10, 0)?.toString() ?: ""
            val finalText = commitProcessor.process(
                CommitContext(caseAdjusted, currentBefore, language)
            ) { ic.deleteSurroundingText(1, 0) }

            ic.commitText(finalText, 1)
            IMEStore.commitEvents.tryEmit(finalText)

            // 重要：如果是 Commit 動作，我們暫時不在此處重置 SessionCap，
            // 讓後面的狀態更新邏輯去處理。
        }

        // --- 第三步：狀態機邏輯更新 ---
        if (capsMode == CaseProcessor.CapsMode.NONE) {
            // 如果 inputType 根本沒要求大寫，強制鎖死為 false
            isSessionStartingAtBoundary = false
        } else if (composing.isEmpty()) {
            val before = ic.getTextBeforeCursor(20, 0)?.toString() ?: ""
            isSessionStartingAtBoundary = caseProcessor.isAtBoundary(before, capsMode)
        } else if (composing.length == 1) {
            val fullBefore = ic.getTextBeforeCursor(21, 0)?.toString() ?: ""
            val actualBefore = if (fullBefore.isNotEmpty()) fullBefore.dropLast(1) else ""
            isSessionStartingAtBoundary = caseProcessor.isAtBoundary(actualBefore, capsMode)
        }
        // 當長度 > 1 時，不再更新 isSessionStartingAtBoundary，直到 Session 結束

        LogObj.trace("InputType: $inputType, SessionCap: $isSessionStartingAtBoundary, Composing: '$composing', CapsMode: $capsMode")

        // --- 第四步：處理 Composing Text ---
        if (composing.isNotEmpty()) {
            val adjustedComposing =
                caseProcessor.applyCase(composing, isSessionStartingAtBoundary, capsMode)
            ic.setComposingText(adjustedComposing, 1)
        } else {
            ic.finishComposingText()
        }

        // --- 第五步：處理候選字 (含預測詞) ---
        val rawList = output.candidates.ifEmpty { emptyList() }
        if (rawList.isEmpty() || isAppExpired) {
            IMEStore.clearCandidate()
            return
        }

        val processedCandidates = mutableListOf<String>()
        val originalIndexMap = mutableListOf<Int>() // 用來記錄 UI 索引對應原始 output 的哪個索引

        rawList.forEachIndexed { index, s ->
            val adjusted = caseProcessor.applyCase(s, isSessionStartingAtBoundary, capsMode)

            // 需求 1：去重判斷
            if (!processedCandidates.contains(adjusted)) {
                processedCandidates.add(adjusted)
                originalIndexMap.add(index)
            }
        }

        LogObj.trace("InputType: $inputType, SessionCap: $isSessionStartingAtBoundary, Original: ${output.candidates[0]}, Adjusted: ${processedCandidates[0]}")
        IMEStore.updateCandidate(processedCandidates, originalIndexMap)
    }
}