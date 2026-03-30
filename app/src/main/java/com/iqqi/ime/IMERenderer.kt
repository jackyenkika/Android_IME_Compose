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

        val lastChar = ctx.beforeCursor.lastOrNull()?.toString()
        if (text in autoSpacePunctuation && lastChar != null && lastChar != " " && lastChar !in autoSpacePunctuation) {
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
    enum class WordStyle { LOWERCASE, CAPITALIZED, UPPERCASE }

    fun getCapsMode(inputType: Int): CapsMode {
        return when {
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS) != 0 -> CapsMode.CHARACTERS
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_WORDS) != 0 -> CapsMode.WORDS
            (inputType and InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0 -> CapsMode.SENTENCES
            else -> CapsMode.NONE
        }
    }

    // 偵測目前 Composing 的樣式，用來覆蓋系統設定
    fun detectWordStyle(text: String): WordStyle {
        if (text.isEmpty()) return WordStyle.LOWERCASE
        val isFirstUpper = text[0].isUpperCase()
        val isAllUpper = text.length > 1 && text.all { it.isUpperCase() }

        return when {
            isAllUpper -> WordStyle.UPPERCASE
            isFirstUpper -> WordStyle.CAPITALIZED
            else -> WordStyle.LOWERCASE
        }
    }

    // 基礎自動判定 (句首/詞首)
    fun isAtBoundary(before: String, mode: CapsMode): Boolean {
        if (mode == CapsMode.NONE) return false
        if (mode == CapsMode.CHARACTERS) return true
        if (before.isEmpty()) return true
        val trimmed = before.trimEnd()
        if (trimmed.isEmpty()) return true
        return when (mode) {
            CapsMode.SENTENCES -> trimmed.last() in listOf('.', '!', '?', '\n')
            CapsMode.WORDS -> before.last().isWhitespace()
            else -> false
        }
    }

    // 核心轉換函數：結合了「自動」與「手動」兩種條件
    fun applyFinalTransform(
        rawText: String,
        isBoundary: Boolean,
        mode: CapsMode,
        manualStyle: WordStyle
    ): String {
        if (rawText.isEmpty()) return rawText

        // 1. 優先處理「全大寫鎖定」 (TYPE_TEXT_FLAG_CAP_CHARACTERS)
        if (mode == CapsMode.CHARACTERS) return rawText.uppercase()

        // 2. 優先處理「使用者手動輸入樣式」 (Manual Override)
        // 只要使用者手動打了大寫，就不管是不是句首，直接跟隨使用者
        if (manualStyle == WordStyle.UPPERCASE) return rawText.uppercase()
        if (manualStyle == WordStyle.CAPITALIZED) return rawText.replaceFirstChar { it.uppercase() }

        // 3. 最後處理「系統自動判定」 (Auto Caps)
        if (isBoundary && mode != CapsMode.NONE) {
            return rawText.replaceFirstChar { it.uppercase() }
        }

        return rawText
    }
}

class IMERenderer(private val ims: InputMethodService) {

    private val commitProcessor = SmartCommitProcessor()
    private val caseProcessor = CaseProcessor()
    private var isAppExpired = false
    private var isSessionStartingAtBoundary = false   // 鎖定當前輸入 Session 是否從大寫邊界開始
    private var currentManualStyle = CaseProcessor.WordStyle.LOWERCASE

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

        if (composing.isNotEmpty()) {
            currentManualStyle = caseProcessor.detectWordStyle(composing)
        }

        // --- 第一步：處理刪除 ---
        if (output.deleteBeforeCursor) {
            ic.finishComposingText()
            DeleteObj.delete(ic)
        }

        // --- 第二步：處理 Commit ---
        // 這樣可以確保文字先上屏，ic.getTextBeforeCursor 才能抓到正確的後續環境
        output.commitText?.let { raw ->
            // 使用「當前 Session 鎖定」的狀態進行轉換
            val caseAdjusted = caseProcessor.applyFinalTransform(
                raw, isSessionStartingAtBoundary, capsMode, currentManualStyle
            )

            val currentBefore = ic.getTextBeforeCursor(10, 0)?.toString() ?: ""
            val finalText =
                commitProcessor.process(CommitContext(caseAdjusted, currentBefore, language)) {
                    ic.deleteSurroundingText(1, 0)
                }

            ic.commitText(finalText, 1)
            IMEStore.commitEvents.tryEmit(finalText)

            // ✅ 關鍵：Commit 完畢，立即歸還（重置）手動狀態
            currentManualStyle = CaseProcessor.WordStyle.LOWERCASE
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
            val adjusted = caseProcessor.applyFinalTransform(
                composing, isSessionStartingAtBoundary, capsMode, currentManualStyle
            )
            ic.setComposingText(adjusted, 1)
        } else {
            ic.finishComposingText()
        }

        // --- 第五步：處理候選字 (含預測詞) ---
        renderCandidates(output = output, mode = capsMode)
    }

    private fun renderCandidates(output: EngineOutput, mode: CaseProcessor.CapsMode) {
        val rawList = output.candidates.ifEmpty { emptyList() }
        if (rawList.isEmpty() || isAppExpired) {
            IMEStore.clearCandidate(); return
        }

        val processedCandidates = mutableListOf<String>()
        val originalIndexMap = mutableListOf<Int>()

        rawList.forEachIndexed { index, s ->
            // 套用同樣的「雙重判定」邏輯
            val adjusted = caseProcessor.applyFinalTransform(
                s, isSessionStartingAtBoundary, mode, currentManualStyle
            )

            if (!processedCandidates.contains(adjusted)) {
                processedCandidates.add(adjusted)
                originalIndexMap.add(index)
            }
        }

        IMEStore.updateCandidate(processedCandidates, originalIndexMap)
    }
}