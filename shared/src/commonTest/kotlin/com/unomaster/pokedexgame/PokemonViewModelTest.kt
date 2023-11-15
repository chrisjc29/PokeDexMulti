package com.unomaster.pokedexgame

import androidx.compose.ui.graphics.ColorFilter
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.unomaster.pokedexgame.domain.PokemonRepositoryImpl
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel
import com.unomaster.pokedexgame.network.PokemonServiceImpl
import io.ktor.client.HttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PokemonViewModelTest {

    private val client = HttpClient()
    private val pokemonService = PokemonServiceImpl(client)

    private val pokemonRepositoryImpl = PokemonRepositoryImpl(
        pokemonService,
        client
    )

    private val pokemonViewModel = PokemonViewModel(
        SavedStateHandle(),
        pokemonRepositoryImpl
    )

    @Test
    fun `when restarting the game _winner and _overlay are reset to default values of false and a ColorFilter`() {
        pokemonViewModel.restartGame("test")

        assertFalse { pokemonViewModel.winner.value }
        assertIs<ColorFilter>(pokemonViewModel.overlay.value)
    }

    @Test
    fun `when a user has selected the correct pokemon the game _winner and _overlay are set to true and null`() {
        pokemonViewModel.handleMultipleItemChoiceState("test", "test")

        assertTrue { pokemonViewModel.winner.value }
        assertEquals(null, pokemonViewModel.overlay.value)

    }

    @Test
    fun `when a user has selected the incorrect pokemon the game _winner and _overlay are set to false and a ColorFilter`() {
        pokemonViewModel.handleMultipleItemChoiceState("test", "something else")

        assertFalse { pokemonViewModel.winner.value }
        assertIs<ColorFilter>(pokemonViewModel.overlay.value)
    }
}