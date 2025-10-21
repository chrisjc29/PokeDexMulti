package com.currantbun.pokedexmulti.data.networkModels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.unomaster.pokedexgame.domain.models.PokemonApiResponse
import com.unomaster.pokedexgame.domain.models.PokemonDetailsResponse


data class CombinedPokemonResponse(
    val pokemonApiResponse: PokemonApiResponse,
    val pokemonDetailsResponse: PokemonDetailsResponse,
    val pokemonUrl: String,
    val multipleChoiceList: MutableState<List<String>> = mutableStateOf(emptyList())
)
