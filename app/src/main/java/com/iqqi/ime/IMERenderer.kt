package com.iqqi.ime

import android.inputmethodservice.InputMethodService
import com.iqqi.core.EngineOutput
import com.iqqi.ime.util.DeleteObj

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
class IMERenderer(
    private val ims: InputMethodService
) {
    fun render(output: EngineOutput) {
        val ic = ims.currentInputConnection ?: return

        //delete
        if (output.deleteBeforeCursor) {
            ic.finishComposingText()   // ← 必須先清避免buffer殘留
            DeleteObj.delete(ic)
        }

        // commit
        output.commitText?.let {
            ic.commitText(it, 1)
            IMEStore.commitEvents.tryEmit(it)
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