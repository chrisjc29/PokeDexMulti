package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.analytics.Analytics

class RecordingAnalytics : Analytics {
    val loggedEventNames = mutableListOf<String>()
    val loggedParams = mutableListOf<Map<String, String>>()
    var lastUserId: String? = null
        private set

    override fun logEvent(name: String, params: Map<String, String>) {
        loggedEventNames += name
        loggedParams += params
    }

    override fun setUserId(id: String?) {
        lastUserId = id
    }
}
