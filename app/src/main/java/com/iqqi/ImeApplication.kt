package com.iqqi

import android.app.Application
import com.iqqi.AppContainer

class ImeApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)
    }
}