package com.unomaster.pokedexgame.domain

import com.unomaster.pokedexgame.domain.models.CombinedPokemonResponse
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun fetchPokemon(url: String): Flow<State<CombinedPokemonResponse, String>>
}