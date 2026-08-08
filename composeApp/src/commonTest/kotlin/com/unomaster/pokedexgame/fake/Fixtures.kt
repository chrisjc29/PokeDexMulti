package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.data.dto.PokemonDetailDto
import com.unomaster.pokedexgame.data.dto.PokemonPageDto
import com.unomaster.pokedexgame.domain.model.PokemonQuestion

// Builders with defaults, so a test names only the field it cares about. This is a file of top-level
// functions, not types, so several living together is fine.

fun pokemonQuestion(
    answerName: String = "Pikachu",
    artworkUrl: String = "https://example.test/pikachu.png",
    choices: List<String> = listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"),
    nextPageUrl: String? = "https://example.test/pokemon?offset=20",
    pokedexNumber: Int = 25,
    types: List<String> = listOf("Electric"),
) = PokemonQuestion(
    answerName = answerName,
    artworkUrl = artworkUrl,
    choices = choices,
    nextPageUrl = nextPageUrl,
    pokedexNumber = pokedexNumber,
    types = types,
)

fun pokemonPageDto(
    next: String? = "https://example.test/pokemon?offset=20",
    names: List<String> = listOf("bulbasaur", "charmander", "squirtle", "pikachu"),
) = PokemonPageDto(
    next = next,
    results = names.map { PokemonPageDto.Entry(name = it, url = "https://example.test/pokemon/$it") },
)

fun pokemonDetailDto(
    id: Int = 1,
    name: String = "bulbasaur",
    artworkUrl: String = "https://example.test/bulbasaur.png",
    types: List<String> = listOf("grass"),
) = PokemonDetailDto(
    id = id,
    name = name,
    sprites = PokemonDetailDto.Sprites(
        other = PokemonDetailDto.Sprites.Other(
            officialArtwork = PokemonDetailDto.Sprites.Other.OfficialArtwork(
                frontDefault = artworkUrl,
            ),
        ),
    ),
    types = types.map {
        PokemonDetailDto.TypeSlot(type = PokemonDetailDto.TypeSlot.Type(name = it))
    },
)
