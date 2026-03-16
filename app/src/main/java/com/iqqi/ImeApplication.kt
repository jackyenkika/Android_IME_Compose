package com.iqqi

import android.app.Application
import android.content.Context
import java.io.File

class ImeApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        copyStickersFromAssets(this)

        container = AppContainer(this)
    }

    fun copyStickersFromAssets(context: Context) {

        val am = context.assets
        val root = "stickers"

        val packs = am.list(root) ?: return

        for (pack in packs) {

            val files = am.list("$root/$pack") ?: continue

            val destDir = File(context.filesDir, "stickers/$pack")
            destDir.mkdirs()

            for (file in files) {

                val dest = File(destDir, file)

                if (dest.exists()) continue

                am.open("$root/$pack/$file").use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}