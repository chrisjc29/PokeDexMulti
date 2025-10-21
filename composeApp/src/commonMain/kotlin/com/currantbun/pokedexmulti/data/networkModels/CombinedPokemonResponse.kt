package com.unomaster.pokedexgame.domain.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap


data class CombinedPokemonResponse(
    val pokemonApiResponse: PokemonApiResponse,
    val pokemonDetailsResponse: PokemonDetailsResponse,
    val pokemonBitmap: MutableState<ImageBitmap?> = mutableStateOf(null),
    val multipleChoiceList: MutableState<List<String>> = mutableStateOf(emptyList())
)
