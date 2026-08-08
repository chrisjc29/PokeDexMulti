package com.unomaster.pokedexgame.analytics

import android.util.Log

class LogcatCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) {
        Log.e("CrashReporter", "recordException", throwable)
    }

    override fun setKey(key: String, value: String) {
        Log.d("CrashReporter", "setKey $key=$value")
    }

    override fun log(message: String) {
        Log.d("CrashReporter", message)
    }
}
