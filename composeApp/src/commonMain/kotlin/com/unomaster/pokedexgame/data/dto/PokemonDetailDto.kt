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
    // Defaulted rather than required: the type label is a nice-to-have on the solved screen, and a
    // response that somehow arrives without it should still produce a playable round rather than a
    // deserialization failure the player sees as "couldn't connect".
    @SerialName("types") val types: List<TypeSlot> = emptyList(),
) {
    @Serializable
    data class TypeSlot(
        @SerialName("type") val type: Type,
    ) {
        @Serializable
        data class Type(
            @SerialName("name") val name: String,
        )
    }

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
