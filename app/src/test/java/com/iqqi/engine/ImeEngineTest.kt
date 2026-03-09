package com.iqqi.engine

import com.iqqi.core.EngineState
import com.iqqi.core.ImeAction
import com.iqqi.core.InputMode
import com.iqqi.dictionary.Dictionary
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.junit.Test

class ImeEngineTest {

    class MockDictionary : Dictionary {
        override fun query(code: String): List<String> {
            return when (code) {
                "ni" -> listOf("你", "尼")
                "hao" -> listOf("好")
                "nihao" -> listOf("你好")
                else -> emptyList()
            }
        }

        fun predict(previous: String): List<String> {
            return when (previous) {
                "你" -> listOf("好", "們")
                else -> emptyList()
            }
        }
    }

    /** ---------- Idle 狀態 ---------- */
    @Test
    fun `Idle state input should start composing`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))

        val outputN = engine.dispatch(ImeAction.Input(Key.Char('n')))
        assertEquals("n", engine.currentState.buffer)
        assertEquals(InputMode.Composing, engine.currentState.mode)
        assertEquals(emptyList<String>(), outputN.candidates)

        val outputI = engine.dispatch(ImeAction.Input(Key.Char('i')))
        assertEquals("ni", engine.currentState.buffer)
        assertEquals(listOf("你", "尼"), outputI.candidates)
        assertEquals(InputMode.Composing, engine.currentState.mode)
    }

    /** ---------- Composing 狀態 ---------- */
    @Test
    fun `Composing input should update buffer and candidates`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))

        engine.dispatch(ImeAction.Input(Key.Char('n')))
        engine.dispatch(ImeAction.Input(Key.Char('i')))
        val output = engine.dispatch(ImeAction.Input(Key.Char('h')))
        assertEquals("nih", engine.currentState.buffer)
        assertEquals(emptyList<String>(), output.candidates)
    }

    @Test
    fun `Composing delete should remove last char`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        engine.dispatch(ImeAction.Input(Key.Char('n')))
        engine.dispatch(ImeAction.Input(Key.Char('i')))
        val output = engine.dispatch(ImeAction.Delete)
        assertEquals("n", engine.currentState.buffer)
        assertEquals(InputMode.Composing, engine.currentState.mode)
        assertEquals(emptyList<String>(), output.candidates)
    }

    @Test
    fun `Composing commit should reset state`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        engine.dispatch(ImeAction.Input(Key.Char('n')))
        engine.dispatch(ImeAction.Input(Key.Char('i')))
        val output = engine.dispatch(ImeAction.Commit)
        assertEquals("", engine.currentState.buffer)
        assertEquals(InputMode.Idle, engine.currentState.mode)
        assertEquals("", engine.currentState.composing)
        assertEquals(emptyList<String>(), output.candidates)
        assertEquals("你", output.commitText) // commit 第一候選
    }

    @Test
    fun `Composing select candidate should reset state and commit`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        engine.dispatch(ImeAction.Input(Key.Char('n')))
        engine.dispatch(ImeAction.Input(Key.Char('i')))
        val output = engine.dispatch(ImeAction.SelectCandidate(1))
        assertEquals("", engine.currentState.buffer)
        assertEquals(InputMode.Idle, engine.currentState.mode)
        assertEquals("", engine.currentState.composing)
        assertEquals(emptyList<String>(), output.candidates)
        assertEquals("尼", output.commitText) // commit 選擇的候選
    }

    /** ---------- Selecting 狀態 ---------- */
    @Test
    fun `Selecting delete should remove last char and go back to composing`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        engine.dispatch(ImeAction.Input(Key.Char('n')))
        engine.dispatch(ImeAction.Input(Key.Char('i')))
        engine.dispatch(ImeAction.SelectCandidate(0)) // 選候選，回到 Idle

        // 模擬刪除 -> 應回 Composing
        val output = engine.dispatch(ImeAction.Delete)
        assertEquals("", engine.currentState.buffer)
        assertEquals(InputMode.Idle, engine.currentState.mode)
        assertEquals("", engine.currentState.composing)
        assertEquals(emptyList<String>(), output.candidates)
    }

    /** ---------- Predicting 模式 ---------- */
    @Test
    fun `Predicting select candidate should commit predicting text`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        // 模擬進入 Predicting
        engine.currentState.mode = InputMode.Predicting
        engine.currentState.predictingCandidates = listOf("好", "們")
        val output = engine.dispatch(ImeAction.SelectCandidate(0))
        assertEquals(InputMode.Idle, engine.currentState.mode)
        assertEquals("好", output.commitText)
    }

    @Test
    fun `Predicting commit should commit first predicting text`() {
        val engine = ImeEngine(CIMReducer(MockDictionary()))
        engine.currentState.mode = InputMode.Predicting
        engine.currentState.predictingCandidates = listOf("好", "們")
        val output = engine.dispatch(ImeAction.Commit)
        assertEquals(InputMode.Idle, engine.currentState.mode)
        assertEquals("好", output.commitText)
    }

    /** ---------- Reducer 單測 ---------- */
    @Test
    fun `reducer input char produces buffer`() {
        val reducer = CIMReducer(MockDictionary())
        val state = EngineState()
        val newState = reducer.reduce(state, ImeAction.Input(Key.Char('n')))
        assertEquals("n", newState.buffer)
    }
}