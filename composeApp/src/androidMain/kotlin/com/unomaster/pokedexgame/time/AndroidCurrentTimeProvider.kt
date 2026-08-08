package com.unomaster.pokedexgame.time

import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider

// Deliberately reads the platform clock directly rather than a kotlinx-datetime Clock: this is the
// one class allowed to know what time it is, and every caller above it is injected with the
// interface so tests stay deterministic.
class AndroidCurrentTimeProvider : CurrentTimeProvider {
    override fun currentEpochSeconds(): Long = System.currentTimeMillis() / 1000
}
