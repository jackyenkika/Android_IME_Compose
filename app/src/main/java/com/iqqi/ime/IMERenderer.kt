package com.iqqi.ime

import android.inputmethodservice.InputMethodService
import com.iqqi.core.EngineOutput
import com.iqqi.ime.util.DeleteObj
import com.iqqi.ime.util.ToolObj.expireTimestamp
import com.iqqi.keyboard.model.KeyboardLanguage

/**
 * EngineOutput → Android API
 * setComposingText()
 * setCandidatesView()
 * commitText()
 *
 * ❌ 不要做
 * 	•	呼叫 engine
 * 	•	修改 state
 * 	•	推測下一步
 */

data class CommitContext(
    val text: String,
    val beforeCursor: String,
    val language: KeyboardLanguage
)

class SmartCommitProcessor {
    private val autoSpacePunctuation = setOf(",", ".", "!", "?", ":", ";")
    private var lastSpaceTimestamp: Long = 0L
    private val DOUBLE_SPACE_INTERVAL = 700L

    fun process(
        ctx: CommitContext,
        onDoubleSpaceTriggered: () -> Unit
    ): String {
        val currentTime = System.currentTimeMillis()
        var text = ctx.text

        if (ctx.language == KeyboardLanguage.ENGLISH) {
            // 情況 A: 這次只輸入一個純空格 (通常是第二次按空格時)
            if (text == " ") {
                val len = ctx.beforeCursor.length
                val lastChar = if (len >= 1) ctx.beforeCursor[len - 1] else null
                val secondLastChar = if (len >= 2) ctx.beforeCursor[len - 2] else null

                val isQuickDoubleTap = (currentTime - lastSpaceTimestamp) < DOUBLE_SPACE_INTERVAL

                if (lastChar == ' ' &&
                    secondLastChar != null &&
                    !autoSpacePunctuation.contains(secondLastChar.toString()) &&
                    secondLastChar != ' ' &&
                    isQuickDoubleTap
                ) {
                    lastSpaceTimestamp = 0L // 觸發後重置
                    onDoubleSpaceTriggered()
                    return ". "
                }
                lastSpaceTimestamp = currentTime
            }
            // 情況 B: 這次輸入的字串是以空格結尾 (例如 "game ")
            else if (text.endsWith(" ")) {
                lastSpaceTimestamp = currentTime
            }
            // 情況 C: 其他字元
            else {
                lastSpaceTimestamp = 0L
            }

            // 標點符號後補空格邏輯
            if (text in autoSpacePunctuation && !ctx.beforeCursor.endsWith(" ")) {
                text += " "
            }
        }
        return text
    }
}

class IMERenderer(
    private val ims: InputMethodService
) {
    private val processor = SmartCommitProcessor()
    private var isAppExpired = false

    init {
        BuildConfig.AppExpireDate.expireTimestamp()
            ?.let { isAppExpired = it < System.currentTimeMillis() }
    }

    fun render(output: EngineOutput, language: KeyboardLanguage) {
        val ic = ims.currentInputConnection ?: return

        //delete
        if (output.deleteBeforeCursor) {
            ic.finishComposingText()   // ← 必須先清避免buffer殘留
            DeleteObj.delete(ic)
        }

        // commit
        output.commitText?.let { raw ->

            val before = ic.getTextBeforeCursor(2, 0)
                ?.toString()
                ?: ""

            val ctx = CommitContext(
                text = raw,
                beforeCursor = before,
                language = language
            )

            val finalText = processor.process(
                ctx = ctx,
                onDoubleSpaceTriggered = {
                    // 關鍵：如果觸發了雙擊空格，就在 commit ". " 之前先刪除畫面上那個空格
                    ic.deleteSurroundingText(1, 0)
                }
            )

            ic.commitText(finalText, 1)

            IMEStore.commitEvents.tryEmit(finalText)
        }

        // composing
        val composing = output.composingText
        if (!composing.isNullOrEmpty()) {
            ic.setComposingText(composing, 1)
        } else {
            ic.finishComposingText()
        }

        // candidate
        if (output.candidates.isEmpty()) {
            IMEStore.clearCandidate()
        } else {
            if (!isAppExpired) {
                IMEStore.updateCandidate(
                    output.candidates, output.selectedIndex
                )
            }
        }
    }
}