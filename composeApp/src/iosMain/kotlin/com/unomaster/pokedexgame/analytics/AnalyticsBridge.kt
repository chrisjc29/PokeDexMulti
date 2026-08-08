package com.unomaster.pokedexgame.analytics

// Implemented in Swift using the Firebase iOS SDK; registered at launch.
interface AnalyticsBridge {
    fun logEvent(name: String, params: Map<String, String>)
    fun setUserId(id: String?)
}
