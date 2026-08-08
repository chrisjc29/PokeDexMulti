package com.unomaster.pokedexgame.analytics

interface CrashReporter {
    fun recordException(throwable: Throwable)
    fun setKey(key: String, value: String)
    fun log(message: String)
}
