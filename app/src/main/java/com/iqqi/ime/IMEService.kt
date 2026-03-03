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
import com.iqqi.core.KeyboardType
import com.iqqi.dictionary.CimDictionary
import com.iqqi.engine.CIMReducer
import com.iqqi.engine.ImeEngine
import com.iqqi.ime.keyboard.ComposeKeyboardView

class IMEService : LifecycleInputMethodService(), ViewModelStoreOwner, SavedStateRegistryOwner {

    private lateinit var engine: ImeEngine
    private lateinit var mapper: AndroidKeyMapper
    private lateinit var imeRender: ImeRenderer

    private var currentLayout: KeyboardType = KeyboardType.QWERTY

    //ViewModelStore Methods
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle
        get() = dispatcher.lifecycle
    override val viewModelStore: ViewModelStore
        get() = store

    //SaveStateRegestry Methods

    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry


    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)

        mapper = AndroidKeyMapper { currentLayout }
        imeRender = ImeRenderer(this@IMEService)
    }

    override fun onCreateInputView(): View {

        val view = ComposeKeyboardView(this)

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
            reducer = CIMReducer(CimDictionary())
        )
    }

    override fun onKeyDown(code: Int, event: KeyEvent): Boolean {
        val imeAction = mapper.map(event) ?: return super.onKeyDown(code, event)
        val output = engine.dispatch(imeAction)
        imeRender.render(output)
        return true
    }

}