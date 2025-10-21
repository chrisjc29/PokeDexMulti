package com.unomaster.pokedexgame.domain.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sprites(
    @SerialName("front_default")
    val frontDefault: String,
    @SerialName("other")
    val other: Other,
)