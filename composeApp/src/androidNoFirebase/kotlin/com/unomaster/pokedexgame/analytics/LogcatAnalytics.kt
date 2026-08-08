package com.unomaster.pokedexgame.analytics

import android.util.Log

class LogcatAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        Log.d("Analytics", "event=$name params=$params")
    }

    override fun setUserId(id: String?) {
        Log.d("Analytics", "setUserId=$id")
    }
}
