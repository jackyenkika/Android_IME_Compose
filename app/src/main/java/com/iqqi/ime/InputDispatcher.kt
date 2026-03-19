package com.iqqi.ime

import com.iqqi.core.ImeAction
import com.iqqi.engine.IMEEngine
import com.iqqi.keyboard.model.KeyboardLanguage

class InputDispatcher(
    private val engine: IMEEngine,
    private val language: KeyboardLanguage,
    private val renderer: IMERenderer
) {

    fun dispatch(action: ImeAction) {
        val output = engine.dispatch(action)
        renderer.render(output,language)
    }

}