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

        // commit
        output.commitText?.let {
            ic.commitText(it, 1)
        }

        // composing
        if (output.composingText != null) {
            ic.setComposingText(output.composingText, 1)
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

        //delete
        if (output.deleteBeforeCursor) {
            DeleteObj.delete(ic)
        }
    }
}