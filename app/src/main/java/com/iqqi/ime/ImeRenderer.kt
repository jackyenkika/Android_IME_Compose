package com.iqqi.ime

import android.inputmethodservice.InputMethodService
import com.iqqi.core.EngineOutput

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
class ImeRenderer(
    private val ims: InputMethodService
) {

    fun render(output: EngineOutput) {
        val ic = ims.currentInputConnection ?: return

        // commitText 優先
        output.commitText?.let {
            ic.commitText(it, 1)
        }

        output.composingText?.let {
            ic.setComposingText(it, 1)
        }

        // candidates → UI
        // 如果是二元聯想模式，優先渲染 predictingCandidates

        // UI 顯示候選列表，例如 RecyclerView / ListView
        // selectedIndex 用於高亮
    }
}