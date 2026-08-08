package com.unomaster.pokedexgame.data.remote

import arrow.core.Either
import com.unomaster.pokedexgame.data.dto.PokemonDetailDto
import com.unomaster.pokedexgame.data.dto.PokemonPageDto
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.network.networkCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorPokemonRemoteSource(
    private val client: HttpClient,
) : PokemonRemoteSource {

    override suspend fun fetchPage(pageUrl: String?): Either<DomainError, PokemonPageDto> =
        networkCall { client.get(pageUrl ?: FIRST_PAGE_URL).body() }

    override suspend fun fetchDetail(detailUrl: String): Either<DomainError, PokemonDetailDto> =
        networkCall { client.get(detailUrl).body() }

    private companion object {
        // The API's own `next` cursor drives every later page, so this is the only literal URL in
        // the app. Nothing above the data layer knows PokeAPI exists.
        const val FIRST_PAGE_URL = "https://pokeapi.co/api/v2/pokemon/"
    }
}
