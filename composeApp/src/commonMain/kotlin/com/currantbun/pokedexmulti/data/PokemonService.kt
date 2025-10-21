package com.unomaster.pokedexgame.network

import com.unomaster.pokedexgame.domain.models.PokemonApiResponse
import com.unomaster.pokedexgame.domain.models.PokemonDetailsResponse

interface PokemonService {
    suspend fun getPokemonList(url: String): Result<PokemonApiResponse>
    suspend fun getPokemonDetails(url: String): Result<PokemonDetailsResponse>
}