package com.unomaster.pokedexgame.domain.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Other(
    @SerialName("official-artwork")
    val officialArtwork: OfficialArtwork
)