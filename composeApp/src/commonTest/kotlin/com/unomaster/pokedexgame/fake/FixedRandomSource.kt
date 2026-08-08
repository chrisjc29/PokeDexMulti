package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.domain.random.RandomSource

// Deterministic by construction: nextInt always returns the same index, and shuffled preserves
// order. That is what lets a repository test assert the exact answer and the exact choice list
// instead of asserting something vague like "contains four names".
class FixedRandomSource(
    private val index: Int = 0,
) : RandomSource {
    override fun nextInt(until: Int): Int = index

    override fun <T> shuffled(items: List<T>): List<T> = items
}
