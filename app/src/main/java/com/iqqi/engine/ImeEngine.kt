package com.iqqi.engine

import com.iqqi.core.EngineOutput
import com.iqqi.core.EngineState
import com.iqqi.core.ImeAction
import com.iqqi.core.InputMode
import com.iqqi.core.Reducer

class ImeEngine(private val reducer: Reducer) {
    private var state = EngineState()
    val currentState: EngineState
        get() = state

    fun dispatch(action: ImeAction): EngineOutput {
        state = reducer.reduce(state, action)
        return EngineOutput(
            composingText = state.composing.ifEmpty { null },
            candidates = if (state.mode == InputMode.Predicting) {
                state.predictingCandidates
            } else {
                state.candidates
            },
            selectedIndex = state.selectedIndex,
            mode = state.mode,
            commitText = state.commitText
        )
    }
}