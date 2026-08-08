package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider

class FixedCurrentTimeProvider(
    private val epochSeconds: Long = 1_000_000L,
) : CurrentTimeProvider {
    override fun currentEpochSeconds(): Long = epochSeconds
}
