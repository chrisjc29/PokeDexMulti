package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider

// A var rather than a val: a test that asserts on elapsed time has to be able to move the clock
// between the two reads, and that is the whole reason the clock is injected in the first place.
class FixedCurrentTimeProvider(
    var epochSeconds: Long = 1_000_000L,
) : CurrentTimeProvider {
    override fun currentEpochSeconds(): Long = epochSeconds
}
