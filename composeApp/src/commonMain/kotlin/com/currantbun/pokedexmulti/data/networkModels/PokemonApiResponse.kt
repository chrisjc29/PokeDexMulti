package com.unomaster.pokedexgame.domain.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonApiResponse(
    @SerialName("count")
    val count: Int,
    @SerialName("next")
    val next: String,
    @SerialName("results")
    val results: List<Result>
)