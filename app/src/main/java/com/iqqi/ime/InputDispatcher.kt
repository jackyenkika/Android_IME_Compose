package com.iqqi.ime

import com.iqqi.core.ImeAction
import com.iqqi.engine.ImeEngine

class InputDispatcher(
    private val engine: ImeEngine,
    private val renderer: IMERenderer
) {

    fun dispatch(action: ImeAction) {
        val output = engine.dispatch(action)
        renderer.render(output)
    }

}