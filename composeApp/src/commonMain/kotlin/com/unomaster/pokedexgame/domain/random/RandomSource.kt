package com.unomaster.pokedexgame.domain.random

// Randomness is a dependency, for the same reason the clock is: a repository that calls
// Random.nextInt() directly cannot be asserted on, so the test either loops or asserts nothing.
// Inject this and a test picks the Pokemon it wants to be the answer.
interface RandomSource {
    fun nextInt(until: Int): Int

    fun <T> shuffled(items: List<T>): List<T>
}
