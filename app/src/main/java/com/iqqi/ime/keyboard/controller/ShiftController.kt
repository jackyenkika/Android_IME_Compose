package com.iqqi.ime.keyboard.controller

import com.iqqi.ime.keyboard.state.ShiftState

class ShiftController {

    private var lastClick = 0L

    fun nextState(current: ShiftState): ShiftState {

        val now = System.currentTimeMillis()

        val result =
            if (now - lastClick < 400) {

                if (current == ShiftState.CAPS_LOCK)
                    ShiftState.OFF
                else
                    ShiftState.CAPS_LOCK

            } else {

                when (current) {
                    ShiftState.OFF -> ShiftState.ON
                    ShiftState.ON -> ShiftState.OFF
                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                }

            }

        lastClick = now

        return result
    }
}