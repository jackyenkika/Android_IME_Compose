package com.iqqi.core

interface Reducer {
    fun reduce(state: EngineState, action: ImeAction): EngineState
}