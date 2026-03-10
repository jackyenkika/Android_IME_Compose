package com.iqqi.ime

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.iqqi.ImeApplication
import com.iqqi.core.ImeAction
import com.iqqi.dictionary.CimDictionary
import com.iqqi.engine.CIMReducer
import com.iqqi.engine.ImeEngine
import com.iqqi.ime.util.DeleteRepeater
import com.iqqi.keyboard.ComposeKeyboardView

class IMEService : LifecycleInputMethodService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var engine: ImeEngine
    private lateinit var mapper: IMEKeyMapper
    private lateinit var imeRender: IMERenderer

    private val deleteRepeater = DeleteRepeater()

    private lateinit var inputDispatcher: InputDispatcher
    //ViewModelStore Methods

    private val container
        get() = (application as ImeApplication).container
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle
    override val viewModelStore: ViewModelStore
        get() = store

    //SaveStateRegestry Methods

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        mapper = IMEKeyMapper()
        imeRender = IMERenderer(this@IMEService)
    }

    override fun onCreateInputView(): View {

        val view = ComposeKeyboardView(
            context = this, repository = container.settingsRepository
        )

        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this)
            decorView.setViewTreeViewModelStoreOwner(this)
            decorView.setViewTreeSavedStateRegistryOwner(this)
        }
        return view
    }


    override fun onStartInput(attributes: EditorInfo, restarting: Boolean) {
        super.onStartInput(attributes, restarting)
        engine = ImeEngine(
            reducer = CIMReducer(this@IMEService, CimDictionary())
        )

        inputDispatcher = InputDispatcher(
            engine = engine,
            renderer = imeRender
        )
    }

    override fun onKeyDown(code: Int, event: KeyEvent): Boolean {
        val action = mapper.map(event)
            ?: return super.onKeyDown(code, event)

        if (action is ImeAction.Delete) {
            onDeleteKeyDown()  // 進入判斷 buffer 或長按刪除
            return true
        }

        dispatch(action)

        return true
    }

    override fun onKeyUp(code: Int, event: KeyEvent): Boolean {
        if (code == KeyEvent.KEYCODE_DEL) {
            onDeleteKeyUp()  // 停止 DeleteRepeater
            return true
        }
        return super.onKeyUp(code, event)
    }
    //=========================================================

    fun dispatch(action: ImeAction) {
        inputDispatcher.dispatch(action)
    }

    fun onDeleteKeyDown() {
        val ic = currentInputConnection ?: return

        // 先判斷 EngineState buffer
        val bufferNotEmpty = engine.currentState.buffer.isNotEmpty() ||
                engine.currentState.composing.isNotEmpty() ||
                engine.currentState.candidates.isNotEmpty() ||
                engine.currentState.predictingCandidates.isNotEmpty()

        if (bufferNotEmpty) {
            // buffer 還有內容 → 走 Engine 刪除
            dispatch(ImeAction.Delete)
        } else {
            // buffer 沒有 → 走長按刪除
            deleteRepeater.singleDelete(ic)
            deleteRepeater.start(ic)
        }
    }

    fun onDeleteKeyUp() {
        deleteRepeater.stop()
    }
    //=========================================================
}