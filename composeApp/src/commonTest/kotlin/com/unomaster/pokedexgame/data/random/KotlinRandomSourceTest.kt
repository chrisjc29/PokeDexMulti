package com.unomaster.pokedexgame.data.random

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinRandomSourceTest {

    // Seeded, so this asserts the delegation is real rather than that randomness is random.
    @Test
    fun delegatesToTheRandomItWasGiven() {
        val expectedIndex = Random(SEED).nextInt(20)
        val expectedOrder = (1..5).toList().shuffled(Random(SEED))

        val source = KotlinRandomSource(Random(SEED))

        assertEquals(expectedIndex, source.nextInt(20))
        assertEquals(expectedOrder, KotlinRandomSource(Random(SEED)).shuffled((1..5).toList()))
    }

    @Test
    fun shufflingKeepsEveryElement() {
        val items = (1..10).toList()

        val shuffled = KotlinRandomSource(Random(SEED)).shuffled(items)

        assertEquals(items.toSet(), shuffled.toSet())
        assertTrue(shuffled.size == items.size)
    }

    private companion object {
        const val SEED = 1234
    }
}
