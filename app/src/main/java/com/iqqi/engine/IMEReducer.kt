package com.iqqi.engine

import android.content.Context
import com.iqqi.core.EngineState
import com.iqqi.core.ImeAction
import com.iqqi.core.InputMode
import com.iqqi.core.Key
import com.iqqi.core.Reducer
import com.iqqi.dictionary.Dictionary
import com.iqqi.dictionary.KikaDictionary
import com.iqqi.ime.util.LogObj
import com.iqqi.keyboard.model.KeyboardLanguage
import java.io.File

class IMEReducer(
    context: Context,
    private val language: KeyboardLanguage,
    private val dict: Dictionary
) : Reducer {

    init {
        val predictDict = if (dict is KikaDictionary) {
            getResourcePath(context)
        } else {
            //CIMDictionary
            context.applicationInfo.dataDir
        }

        val userDataDict = if (dict is KikaDictionary) {
            context.applicationInfo.dataDir
        } else {
            //CIMDictionary
            null
        }

        val engineInitStatus = dict.init(predictDict = predictDict, userDataDict = userDataDict)
        LogObj.trace("predictDict = $predictDict , userDataDict = $userDataDict , engineInitStatus = $engineInitStatus")
    }

    private fun getResourcePath(context: Context): String {
        val libraryPaths = System.getProperty("java.library.path")
            ?.split(":")
            ?: emptyList()

        val path = libraryPaths.firstOrNull {
            File("$it/libIQQILib.so").exists()
        }

        return path ?: context.applicationInfo.nativeLibraryDir
    }

    override fun reduce(state: EngineState, action: ImeAction): EngineState {
        LogObj.debug("reduce start state = $state , action = $action")
        val endState = when (state.mode) {
            InputMode.Idle -> handleIdle(state, action)
            InputMode.Composing -> handleComposing(state, action)
            InputMode.Selecting -> handleSelecting(state, action)
            InputMode.Predicting -> handlePredicting(state, action)
        }
        LogObj.debug("reduce end state = $endState , action = $action")
        return endState
    }

    // ---------------- Idle ----------------

    private fun handleIdle(state: EngineState, action: ImeAction): EngineState {
        return when (action) {

            is ImeAction.Input -> when (val key = action.key) {

                is Key.Char -> {
                    if (isSpellingChar(key.c)) {
                        buildComposingState(
                            state,
                            state.buffer + key.c
                        )
                    } else {
                        EngineState(
                            commitText = key.c.toString()
                        )
                    }
                }

                is Key.Text -> {
                    EngineState(
                        commitText = key.text
                    )
                }

                Key.Space -> {
                    if (isNeedAppendSpace() && state.lastTextWasSpace()) {
                        EngineState(
                            deleteBeforeCursor = true, // 刪掉上一個空格
                            commitText = ". ",
                            mode = InputMode.Idle
                        )
                    } else {
                        commit(" ")
                    }
                }

                Key.Enter -> {
                    commit("\n")
                }
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }

            is ImeAction.SelectCandidate -> state
        }
    }

    // ---------------- Composing ----------------

    private fun handleComposing(state: EngineState, action: ImeAction): EngineState {

        return when (action) {

            is ImeAction.Input -> handleComposingInput(state, action)

            is ImeAction.SelectCandidate -> {
                commitAndPredict(state, action.index, isNeedAppendSpace())
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }
        }
    }

    private fun handleComposingInput(
        state: EngineState,
        action: ImeAction.Input
    ): EngineState {

        return when (val key = action.key) {

            is Key.Char -> {

                if (isSpellingChar(key.c)) {

                    buildComposingState(
                        state,
                        state.buffer + key.c
                    )

                } else {

                    // 先 commit 現有 composing
                    val commit = state.candidates.getOrNull(state.selectedIndex)
                        ?: state.composing

                    EngineState(
                        commitText = commit + key.c,
                        mode = InputMode.Idle
                    )
                }
            }

            is Key.Text -> {

                val commit = state.candidates.getOrNull(state.selectedIndex)
                    ?: state.composing

                EngineState(
                    commitText = commit + key.text,
                    mode = InputMode.Idle
                )
            }

            is Key.Space -> {
                commitAndPredict(state, 0, isNeedAppendSpace())
            }

            is Key.Enter -> {
                if (state.composing.isEmpty()) {
                    commit("\n")
                } else {
                    var text = state.composing
                    if (language == KeyboardLanguage.CHINESE) {
                        text = text.replace("'", "")
                    }
                    commit(text)
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
                commitAndPredict(state, action.index, isNeedAppendSpace())
            }

            is ImeAction.Delete -> {
                handleDelete(state)
            }

            is ImeAction.Input -> when (val key = action.key) {
                is Key.Text -> {
                    EngineState(commitText = key.text)
                }

                else -> state
            }
        }
    }

    // ---------------- Predicting ----------------

    private fun handlePredicting(
        state: EngineState,
        action: ImeAction
    ): EngineState {

        return when (action) {

            is ImeAction.SelectCandidate -> {
                var commit = state.predictingCandidates.getOrNull(action.index)
                    ?: return EngineState()

                if (isNeedAppendSpace()) {
                    commit = "$commit "
                }
                val predict = dict.predict(commit.trim())

                commit(
                    text = commit,
                    nextMode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting,
                    predict = predict
                )
            }

            ImeAction.Delete -> {
                EngineState(
                    deleteBeforeCursor = true
                )
            }

            is ImeAction.Input -> when (val key = action.key) {
                is Key.Char -> {
                    if (isSpellingChar(key.c)) {
                        buildComposingState(
                            EngineState(),
                            key.c.toString()
                        )
                    } else {
                        EngineState(
                            commitText = key.c.toString()
                        )
                    }
                }

                is Key.Text -> {
                    EngineState(
                        commitText = key.text
                    )
                }

                Key.Space -> {
                    if (language == KeyboardLanguage.ENGLISH) {
                        commit(text = " ")
                    } else {
                        //選取預測候選字的邏輯
                        var commit = state.predictingCandidates.firstOrNull()
                            ?: return EngineState(commitText = " ")

                        if (isNeedAppendSpace()) {
                            commit = "$commit "
                        }

                        val predict = dict.predict(commit.trim())

                        commit(
                            text = commit,
                            nextMode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting,
                            predict = predict
                        )
                    }
                }

                Key.Enter -> {
                    commit(text = "\n")
                }
            }

        }
    }

    // ---------------- Shared helpers ----------------


    private fun isSpellingChar(c: Char): Boolean {
        return when (language) {
            KeyboardLanguage.ENGLISH -> c.isLetter() // A-Z,a-z
            KeyboardLanguage.CHINESE -> c.isLetter()
            // 可以擴展其他語言
            else -> c.isLetter()
        }
    }

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
        index: Int,
        appendSpace: Boolean = false
    ): EngineState {

        var commit = state.candidates.getOrNull(index)
            ?: state.composing

        if (commit.isEmpty()) {
            return EngineState()
        }

        if (appendSpace) {
            commit += " "
        }

        val predict = dict.predict(commit.trim())

        return commit(
            text = commit,
            nextMode = if (predict.isEmpty()) InputMode.Idle else InputMode.Predicting,
            predict = predict
        )
    }

    private fun handleDelete(state: EngineState): EngineState {

        // 有 buffer -> 刪 buffer
        if (state.buffer.isNotEmpty()) {

            val newBuffer = state.buffer.dropLast(1)

            if (newBuffer.isEmpty()) {
                return EngineState(
                    deleteBeforeCursor = true
                )
            }

            val candidates = dict.query(newBuffer)
            val composing = dict.composing(newBuffer)

            return state.copy(
                buffer = newBuffer,
                composing = composing,
                candidates = candidates,
                selectedIndex = 0,
                mode = InputMode.Composing
            )
        }

        // 沒 buffer -> 刪 editor 字
        return EngineState(
            deleteBeforeCursor = true
        )
    }


    private fun commit(
        text: String,
        nextMode: InputMode = InputMode.Idle,
        predict: List<String> = emptyList()
    ): EngineState {
        return EngineState(
            commitText = text,
            predictingCandidates = predict,
            mode = nextMode
        )
    }

    private fun isNeedAppendSpace(): Boolean {
        return when (language) {
            KeyboardLanguage.ENGLISH -> true
            else -> false
        }
    }

    private fun EngineState.lastTextWasSpace(): Boolean {
        // 判斷上一次 commit 到系統的文字是否為單一空格
        return this.commitText == " "
    }
}