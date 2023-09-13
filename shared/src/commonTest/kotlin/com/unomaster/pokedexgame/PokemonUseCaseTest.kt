package com.unomaster.pokedexgame

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.unomaster.pokedexgame.domain.PokemonRepository
import com.unomaster.pokedexgame.domain.PokemonUseCase
import com.unomaster.pokedexgame.domain.network.PokemonService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PokemonUseCaseTest {

    private val pokemonService = PokemonService()

    private val pokemonRepository = PokemonRepository(
        pokemonService
    )

    private val scope = CoroutineScope(Dispatchers.Main)

    private val pokemonUseCase = PokemonUseCase(
        pokemonRepository,
        scope
    )

    @Test
    fun `when restarting the game _winner and _overlay are reset to default values of false and a ColorFilter`() {
        pokemonUseCase.restartGame("test")

        assertFalse { pokemonUseCase._winner.value }
        assertIs<ColorFilter>(pokemonUseCase._overlay.value)
    }

    @Test
    fun `when a user has selected the correct pokemmon the game _winner and _overlay are set to true and null`() {
        pokemonUseCase.handleMultipleItemChoiceState("test", "test")

        assertTrue { pokemonUseCase._winner.value }
        assertEquals(null, pokemonUseCase._overlay.value)

    }

    @Test
    fun `when a user has selected the incorrect pokemmon the game _winner and _overlay are set to false and a ColorFilter`() {
        pokemonUseCase.handleMultipleItemChoiceState("test", "something else")

        assertFalse { pokemonUseCase._winner.value }
        assertIs<ColorFilter>(pokemonUseCase._overlay.value)
    }
}