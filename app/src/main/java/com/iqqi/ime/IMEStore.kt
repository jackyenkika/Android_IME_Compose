package com.iqqi.ime

import com.iqqi.ime.util.LogObj
import com.iqqi.keyboard.state.CandidateState
import com.iqqi.keyboard.state.KeyboardState
import com.iqqi.keyboard.state.StickerUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

object IMEStore {

    private val _keyboardState = MutableStateFlow(KeyboardState())
    val keyboardState: StateFlow<KeyboardState> = _keyboardState

    private val _candidateState = MutableStateFlow(CandidateState())
    val candidateState: StateFlow<CandidateState> = _candidateState

    private val _stickerState = MutableStateFlow(StickerUiState())

    val stickerState: StateFlow<StickerUiState> = _stickerState

    val commitEvents = MutableSharedFlow<String>(
        extraBufferCapacity = 64
    )
    //===========================================

    fun updateKeyboardState(newState: KeyboardState) {
        _keyboardState.value = newState
    }

    //===========================================

    fun updateCandidate(candidates: List<String>, indexMap: List<Int>) {
        _candidateState.update {
            it.copy(
                candidates = candidates,
                indexMap = indexMap
            )
        }
    }

    fun clearCandidate() {
        _candidateState.value = CandidateState()
    }

    //===========================================

    fun updateStickerState(show: Boolean) {
        LogObj.trace("showStickerPanel = $show")
        _stickerState.update { it.copy(visible = show) }
    }
    //===========================================
}