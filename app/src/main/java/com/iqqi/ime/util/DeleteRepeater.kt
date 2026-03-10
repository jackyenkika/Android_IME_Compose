package com.iqqi.ime.util


import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputConnection

class DeleteRepeater {

    private val handler = Handler(Looper.getMainLooper())

    private var repeatCount = 0
    private var running = false
    private var ic: InputConnection? = null

    private val deleteRunnable = object : Runnable {
        override fun run() {

            val connection = ic ?: return

            val times = deleteBatchSize()

            repeat(times) {
                DeleteObj.delete(connection)
            }

            repeatCount++

            handler.postDelayed(this, currentDelay())
        }
    }

    fun start(ic: InputConnection) {

        if (running) return

        running = true
        this.ic = ic
        repeatCount = 0

        handler.postDelayed(deleteRunnable, 200)
    }

    fun stop() {

        running = false
        ic = null
        handler.removeCallbacks(deleteRunnable)
    }

    fun singleDelete(ic: InputConnection) {
        DeleteObj.delete(ic)
    }

    private fun deleteBatchSize(): Int {

        return when (repeatCount) {
            0 -> 1
            1 -> 2
            2 -> 4
            3 -> 8
            else -> 16
        }
    }

    private fun currentDelay(): Long {

        return when (repeatCount) {
            0 -> 120
            1 -> 100
            2 -> 80
            3 -> 60
            else -> 40
        }
    }
}