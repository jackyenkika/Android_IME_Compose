package com.iqqi.ime

import com.iqqi.keyboard.state.CandidateState
import com.iqqi.keyboard.state.KeyboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object IMEStore {

    private val _keyboardState = MutableStateFlow(KeyboardState())
    val keyboardState: StateFlow<KeyboardState> = _keyboardState

    private val _candidateState = MutableStateFlow(CandidateState())
    val candidateState: StateFlow<CandidateState> = _candidateState


    fun updateKeyboardState(newState: KeyboardState) {
        _keyboardState.value = newState
    }

    fun updateCandidate(candidates: List<String>, selectedIndex: Int) {
        _candidateState.update {
            it.copy(
                candidates = candidates,
                selectedIndex = selectedIndex
            )
        }
    }

    fun clearCandidate() {
        _candidateState.value = CandidateState()
    }
}