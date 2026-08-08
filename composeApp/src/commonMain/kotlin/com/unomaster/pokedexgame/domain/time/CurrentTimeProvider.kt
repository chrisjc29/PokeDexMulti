package com.unomaster.pokedexgame.domain.time

interface CurrentTimeProvider {
    fun currentEpochSeconds(): Long
}
