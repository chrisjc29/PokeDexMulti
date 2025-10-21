package com.currantbun.pokedexmulti.data

import com.currantbun.pokedexmulti.data.networkModels.CombinedPokemonResponse
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {
    suspend fun fetchPokemon(url: String): Flow<State<CombinedPokemonResponse, String>>
}