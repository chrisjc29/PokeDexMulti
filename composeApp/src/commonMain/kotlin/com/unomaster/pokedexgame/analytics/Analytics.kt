package com.unomaster.pokedexgame.analytics

interface Analytics {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun setUserId(id: String?)
}
