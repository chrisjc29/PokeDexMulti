package com.unomaster.pokedexgame

import androidx.compose.ui.graphics.ColorFilter
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.unomaster.pokedexgame.domain.PokemonRepository
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel
import com.unomaster.pokedexgame.domain.network.PokemonService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PokemonViewModelTest {

    private val pokemonService = PokemonService()

    private val pokemonRepository = PokemonRepository(
        pokemonService
    )

    private val pokemonViewModel = PokemonViewModel(
        SavedStateHandle(),
        pokemonRepository
    )

    @Test
    fun `when restarting the game _winner and _overlay are reset to default values of false and a ColorFilter`() {
        pokemonViewModel.restartGame("test")

        assertFalse { pokemonViewModel._winner.value }
        assertIs<ColorFilter>(pokemonViewModel._overlay.value)
    }

    @Test
    fun `when a user has selected the correct pokemmon the game _winner and _overlay are set to true and null`() {
        pokemonViewModel.handleMultipleItemChoiceState("test", "test")

        assertTrue { pokemonViewModel._winner.value }
        assertEquals(null, pokemonViewModel._overlay.value)

    }

    @Test
    fun `when a user has selected the incorrect pokemmon the game _winner and _overlay are set to false and a ColorFilter`() {
        pokemonViewModel.handleMultipleItemChoiceState("test", "something else")

        assertFalse { pokemonViewModel._winner.value }
        assertIs<ColorFilter>(pokemonViewModel._overlay.value)
    }
}