package com.unomaster.pokedexgame.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The wire shape of GET /pokemon/{id}. Only the fields the game uses are modelled; the client is
// configured with ignoreUnknownKeys, so the rest of this very large payload is discarded.
@Serializable
data class PokemonDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("sprites") val sprites: Sprites,
) {
    @Serializable
    data class Sprites(
        @SerialName("other") val other: Other,
    ) {
        @Serializable
        data class Other(
            @SerialName("official-artwork") val officialArtwork: OfficialArtwork,
        ) {
            @Serializable
            data class OfficialArtwork(
                @SerialName("front_default") val frontDefault: String,
            )
        }
    }
}
