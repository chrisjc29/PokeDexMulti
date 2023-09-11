package com.unomaster.pokedexgame.domain.network

import com.unomaster.pokedexgame.domain.models.PokemonApiResponse
import com.unomaster.pokedexgame.domain.models.PokemonDetailsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get

class PokemonService {
    suspend fun getPokemonList(url: String): Result<PokemonApiResponse> =
        NetworkDependencies.client.getResult(url)

    suspend fun getPokemonDetails(url: String): Result<PokemonDetailsResponse> =
        NetworkDependencies.client.getResult(url)


    private suspend inline fun <reified R> HttpClient.getResult(
        urlString: String,
        builder: HttpRequestBuilder.() -> Unit = {}
    ): Result<R> = runCatching { get(urlString, builder).body() }
}