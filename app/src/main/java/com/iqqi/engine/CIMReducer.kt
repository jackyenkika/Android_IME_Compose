package com.iqqi.engine

import android.content.Context
import com.iqqi.core.EngineState
import com.iqqi.core.ImeAction
import com.iqqi.core.InputMode
import com.iqqi.core.Key
import com.iqqi.core.Reducer
import com.iqqi.dictionary.Dictionary
import com.iqqi.util.LogObj

class CIMReducer(
    context: Context,
    private val dict: Dictionary
) : Reducer {

    init {
        val userDataDict = context.applicationInfo.dataDir
        val engineInitStatus = dict.init(userDataDict)
        LogObj.trace("userDataDict = $userDataDict , engineInitStatus = $engineInitStatus")
    }

    override fun reduce(state: EngineState, action: ImeAction): EngineState {
        return when (state.mode) {
            InputMode.Idle -> handleIdle(state, action)
            InputMode.Composing -> handleComposing(state, action)
            InputMode.Selecting -> handleSelecting(state, action)
            InputMode.Predicting -> handlePredicting(state, action)
        }
    }

    // ---------------- Idle ----------------

    private fun handleIdle(state: EngineState, action: ImeAction): EngineState {
        return when (action) {

            is ImeAction.Input -> when (val key = action.key) {

                is Key.Char -> buildComposingState(
                    state,
                    state.buffer + key.c
                )

                Key.Space -> {
                    EngineState(
                        commitText = " "
                    )
                }

                Key.Enter -> {
                    EngineState(
                        commitText = "\n"
                    )
                }
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }

            is ImeAction.Commit -> state
            is ImeAction.SelectCandidate -> state
        }
    }

    // ---------------- Composing ----------------

    private fun handleComposing(state: EngineState, action: ImeAction): EngineState {

        return when (action) {

            is ImeAction.Input -> handleComposingInput(state, action)

            is ImeAction.SelectCandidate -> {
                commitAndPredict(state, action.index)
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }

            is ImeAction.Commit -> {
                commitAndPredict(state, state.selectedIndex)
            }

        }
    }

    private fun handleComposingInput(
        state: EngineState,
        action: ImeAction.Input
    ): EngineState {

        return when (val key = action.key) {

            is Key.Char -> {
                buildComposingState(
                    state,
                    state.buffer + key.c
                )
            }

            is Key.Space -> {
                commitAndPredict(state, 0)
            }

            is Key.Enter -> {
                if (state.composing.isEmpty()) {
                    EngineState(commitText = "\n")
                } else {
                    EngineState(commitText = state.composing.replace("'", ""))
                }
            }
        }
    }

    // ---------------- Selecting ----------------

    private fun handleSelecting(
        state: EngineState,
        action: ImeAction
    ): EngineState {

        return when (action) {

            is ImeAction.SelectCandidate -> {
                commitAndPredict(state, action.index)
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }

            is ImeAction.Commit -> {
                commitDirect(state)
            }

            else -> state
        }
    }

    // ---------------- Predicting ----------------

    private fun handlePredicting(
        state: EngineState,
        action: ImeAction
    ): EngineState {

        return when (action) {

            is ImeAction.SelectCandidate -> {
                val commit = state.predictingCandidates.getOrNull(action.index)
                    ?: return EngineState()

                val predict = dict.predict(commit)

                EngineState(
                    commitText = commit,
                    predictingCandidates = predict,
                    mode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting
                )
            }

            ImeAction.Commit -> {
                val commit = state.predictingCandidates.firstOrNull()
                EngineState(commitText = commit)
            }

            ImeAction.Delete -> {
                EngineState(
                    deleteBeforeCursor = true
                )
            }

            is ImeAction.Input -> when (val key = action.key) {

                is Key.Char -> buildComposingState(
                    EngineState(),
                    key.c.toString()
                )

                Key.Space -> {
                    val commit = state.predictingCandidates.firstOrNull()
                        ?: return EngineState(commitText = " ")

                    val predict = dict.predict(commit)

                    EngineState(
                        commitText = commit,
                        predictingCandidates = predict,
                        mode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting
                    )
                }

                Key.Enter -> {
                    EngineState(commitText = "\n")
                }
            }

        }
    }

    // ---------------- Shared helpers ----------------

    private fun buildComposingState(
        state: EngineState,
        buffer: String
    ): EngineState {

        if (buffer.isEmpty()) {
            return state.copy(
                buffer = "",
                composing = "",
                candidates = emptyList(),
                mode = InputMode.Idle
            )
        }

        val candidates = dict.query(buffer)
        val composing = dict.composing(buffer)

        return state.copy(
            buffer = buffer,
            composing = composing,
            candidates = candidates,
            selectedIndex = 0,
            mode = InputMode.Composing
        )
    }

    private fun commitAndPredict(
        state: EngineState,
        index: Int
    ): EngineState {

        val commit = state.candidates.getOrNull(index)
            ?: state.composing

        if (commit.isEmpty()) {
            return EngineState()
        }

        val predict = dict.predict(commit)

        return EngineState(
            commitText = commit,
            predictingCandidates = predict,
            mode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting
        )
    }

    private fun commitDirect(state: EngineState): EngineState {

        val commit = state.candidates.getOrNull(state.selectedIndex)
            ?: state.composing

        if (commit.isEmpty()) {
            return EngineState()
        }

        return EngineState(
            commitText = commit
        )
    }

    private fun handleDelete(state: EngineState): EngineState {

        // 有 buffer -> 刪 buffer
        if (state.buffer.isNotEmpty()) {

            val newBuffer = state.buffer.dropLast(1)

            if (newBuffer.isEmpty()) {
                return EngineState()
            }

            val candidates = dict.query(newBuffer)
            val composing = dict.composing(newBuffer)

            return state.copy(
                buffer = newBuffer,
                composing = composing,
                candidates = candidates,
                selectedIndex = 0,
                mode = InputMode.Composing,
                deleteBeforeCursor = false
            )
        }

        // 沒 buffer -> 刪 editor 字
        return EngineState(
            deleteBeforeCursor = true
        )
    }
}