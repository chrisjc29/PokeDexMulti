package com.unomaster.pokedexgame.data.random

import com.unomaster.pokedexgame.domain.random.RandomSource
import kotlin.random.Random

// The one class allowed to be non-deterministic. Everything above it is injected with the
// RandomSource interface, so tests stay repeatable.
class KotlinRandomSource(
    private val random: Random = Random.Default,
) : RandomSource {

    override fun nextInt(until: Int): Int = random.nextInt(until)

    override fun <T> shuffled(items: List<T>): List<T> = items.shuffled(random)
}
