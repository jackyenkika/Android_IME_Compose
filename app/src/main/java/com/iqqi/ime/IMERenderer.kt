package com.iqqi.ime

import android.inputmethodservice.InputMethodService
import com.iqqi.core.EngineOutput
import com.iqqi.ime.util.DeleteObj
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

    private val autoSpacePunctuation =
        setOf(",", ".", "!", "?", ":", ";")

    fun process(
        ctx: CommitContext
    ): String {

        var text = ctx.text

        // Auto-space after punctuation
        if (ctx.language == KeyboardLanguage.ENGLISH &&
            text in autoSpacePunctuation &&
            ctx.beforeCursor != " "
        ) {
            text += " "
        }

        return text
    }
}

class IMERenderer(
    private val ims: InputMethodService
) {
    private val processor = SmartCommitProcessor()

    fun render(output: EngineOutput, language: KeyboardLanguage) {
        val ic = ims.currentInputConnection ?: return

        //delete
        if (output.deleteBeforeCursor) {
            ic.finishComposingText()   // ← 必須先清避免buffer殘留
            DeleteObj.delete(ic)
        }

        // commit
        output.commitText?.let { raw ->

            val before = ic.getTextBeforeCursor(1, 0)
                ?.toString()
                ?: ""

            val ctx = CommitContext(
                text = raw,
                beforeCursor = before,
                language = language
            )

            val finalText = processor.process(ctx)

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
            IMEStore.updateCandidate(
                output.candidates, output.selectedIndex
            )
        }
    }
}