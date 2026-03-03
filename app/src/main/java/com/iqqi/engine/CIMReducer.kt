package com.iqqi.engine

import com.iqqi.core.EngineState
import com.iqqi.core.ImeAction
import com.iqqi.core.InputMode
import com.iqqi.core.Key
import com.iqqi.core.Reducer
import com.iqqi.dictionary.Dictionary

class CIMReducer(
    private val dict: Dictionary
) : Reducer {

    override fun reduce(state: EngineState, action: ImeAction): EngineState {
        return when (state.mode) {
            InputMode.Idle -> handleIdle(state, action)
            InputMode.Composing -> handleComposing(state, action)
            InputMode.Selecting -> handleSelecting(state, action)
            InputMode.Predicting -> handlePredicting(state, action)
        }
    }

    private fun handleIdle(state: EngineState, action: ImeAction): EngineState {
        return when (action) {
            is ImeAction.Input -> {
                when (val key = action.key) {
                    is Key.Char -> {
                        val buffer = state.buffer + key.c
                        val candidates = dict.query(buffer)
                        state.copy(
                            buffer = buffer,
                            composing = candidates.firstOrNull() ?: buffer,
                            candidates = candidates,
                            mode = InputMode.Composing,
                            selectedIndex = 0
                        )
                    }

                    Key.Space, Key.Enter -> state
                }
            }

            else -> state
        }
    }

    private fun handleComposing(state: EngineState, action: ImeAction): EngineState {
        return when (action) {
            is ImeAction.SelectCandidate -> {
                val commit = state.candidates.getOrNull(action.index) ?: state.composing
                state.copy(
                    buffer = "",
                    composing = "",
                    candidates = emptyList(),
                    mode = if (commit.isEmpty()) InputMode.Idle else InputMode.Predicting,
                    selectedIndex = 0,
                    commitText = commit,
                    predictingCandidates = dict.predict(commit)
                )
            }

            Key.Space -> {
                val commit = state.candidates.getOrNull(0) ?: state.composing
                state.copy(
                    buffer = "",
                    composing = "",
                    candidates = emptyList(),
                    mode = if (commit.isEmpty()) InputMode.Idle else InputMode.Predicting,
                    selectedIndex = 0,
                    commitText = commit,
                    predictingCandidates = dict.predict(commit)
                )
            }

            Key.Space, Key.Enter, is ImeAction.Commit -> {
                state.copy(
                    buffer = "",
                    composing = "",
                    candidates = emptyList(),
                    mode = InputMode.Idle,
                    selectedIndex = 0,
                    commitText = state.candidates.getOrNull(state.selectedIndex) ?: state.composing
                )
            }


            is ImeAction.Input -> {
                when (val key = action.key) {
                    is Key.Char -> {
                        val buffer = state.buffer + key.c
                        val candidates = dict.query(buffer)
                        state.copy(
                            buffer = buffer,
                            composing = candidates.firstOrNull() ?: buffer,
                            candidates = candidates,
                            mode = InputMode.Composing,
                            selectedIndex = 0
                        )
                    }

                    Key.Space, Key.Enter -> EngineState() // commit 清空
                }
            }

            ImeAction.Delete -> {
                val newBuffer = state.buffer.dropLastOrNull() ?: ""
                val candidates = if (newBuffer.isNotEmpty()) dict.query(newBuffer) else emptyList()
                state.copy(
                    buffer = newBuffer,
                    composing = candidates.firstOrNull() ?: "",
                    candidates = candidates,
                    mode = if (newBuffer.isNotEmpty()) InputMode.Composing else InputMode.Idle,
                    selectedIndex = 0
                )
            }

            else -> state
        }
    }

    private fun handleSelecting(state: EngineState, action: ImeAction): EngineState {
        return when (action) {
            is ImeAction.SelectCandidate -> EngineState()
            ImeAction.Delete -> {
                val newBuffer = state.buffer.dropLastOrNull() ?: ""
                val candidates = if (newBuffer.isNotEmpty()) dict.query(newBuffer) else emptyList()
                state.copy(
                    buffer = newBuffer,
                    composing = candidates.firstOrNull() ?: "",
                    candidates = candidates,
                    mode = if (newBuffer.isNotEmpty()) InputMode.Composing else InputMode.Idle,
                    selectedIndex = 0
                )
            }

            ImeAction.Commit -> EngineState()
            else -> state
        }
    }

    private fun handlePredicting(state: EngineState, action: ImeAction): EngineState {
        return when (action) {
            is ImeAction.SelectCandidate -> {
                // 選中二元聯想候選，commitText
                val selected = state.predictingCandidates.getOrNull(action.index)
                EngineState(commitText = selected)
            }

            ImeAction.Delete -> {
                // 取消二元聯想，回到 Composing
                state.copy(
                    mode = InputMode.Composing,
                    predictingCandidates = emptyList(),
                    selectedIndex = 0
                )
            }

            ImeAction.Commit -> {
                // commit 第一個二元聯想候選
                val selected = state.predictingCandidates.firstOrNull()
                EngineState(commitText = selected)
            }

            else -> state
        }
    }

    // 安全用法
    private fun String.dropLastOrNull(): String? = if (isNotEmpty()) dropLast(1) else null
}