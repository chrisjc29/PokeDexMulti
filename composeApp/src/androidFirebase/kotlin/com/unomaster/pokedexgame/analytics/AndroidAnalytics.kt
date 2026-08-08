package com.unomaster.pokedexgame.analytics

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

class AndroidAnalytics : Analytics {
    private val firebase = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, String>) {
        firebase.logEvent(name) {
            params.forEach { (key, value) -> param(key, value) }
        }
    }

    override fun setUserId(id: String?) {
        firebase.setUserId(id)
    }
}
