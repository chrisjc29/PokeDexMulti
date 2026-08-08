package com.unomaster.pokedexgame.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The wire shape of GET /pokemon — a page of name/url pairs plus a cursor to the next page.
// Kept separate from PokemonQuestion so the API's shape can change without leaking into domain.
@Serializable
data class PokemonPageDto(
    @SerialName("next") val next: String? = null,
    @SerialName("results") val results: List<Entry> = emptyList(),
) {
    @Serializable
    data class Entry(
        @SerialName("name") val name: String,
        @SerialName("url") val url: String,
    )
}
