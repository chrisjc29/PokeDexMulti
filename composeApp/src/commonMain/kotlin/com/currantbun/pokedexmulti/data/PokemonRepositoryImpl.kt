package com.unomaster.pokedexgame.domain

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.unomaster.pokedexgame.domain.models.CombinedPokemonResponse
import com.unomaster.pokedexgame.domain.models.PokemonApiResponse
import com.unomaster.pokedexgame.domain.models.PokemonDetailsResponse
import com.unomaster.pokedexgame.network.PokemonService
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class PokemonRepositoryImpl(
    private val pokemonService: PokemonService,
    private val client: HttpClient,
): PokemonRepository {
    override suspend fun fetchPokemon(url: String) = flow {
        emit(State.Loading)
        pokemonService.getPokemonList(url).onSuccess { safePokemonListRequest ->
            val randomPosition = Random.nextInt(0, 20)

            val pokemonDetailsUrl = safePokemonListRequest.results[randomPosition].url
            val pokemonDetailsName = safePokemonListRequest.results[randomPosition].name

            val multipleChoiceList =
                getMultipleChoiceList(safePokemonListRequest, pokemonDetailsName)

            pokemonService.getPokemonDetails(pokemonDetailsUrl)
                .onSuccess { safePokemonDetailsResponse ->
                    val combinedPokemonResponse = combinedPokemonResponse(
                        safePokemonListRequest,
                        safePokemonDetailsResponse,
                        multipleChoiceList,
                        client
                    )

                    emit(State.Success(combinedPokemonResponse))
                }.onFailure {
                    emit(State.Error(it.message.orEmpty()))
                }
        }.onFailure {
            emit(State.Error(it.message.orEmpty()))
        }

    }
}

private suspend fun combinedPokemonResponse(
    pokemonApiResponse: PokemonApiResponse,
    pokemonDetailsResponse: PokemonDetailsResponse,
    multipleChoiceList: List<String>,
    client: HttpClient
) = CombinedPokemonResponse(
    pokemonApiResponse,
    pokemonDetailsResponse,
    pokemonBitmap = mutableStateOf(
        loadImageFromUrl(pokemonDetailsResponse.sprites.other.officialArtwork.frontDefault, client),
    ),
    multipleChoiceList = mutableStateOf(
        multipleChoiceList
    )
)

private fun getMultipleChoiceList(
    pokemonApiResponse: PokemonApiResponse,
    pokemonDetailsName: String
): List<String> {
    val groupedResults = pokemonApiResponse.results.groupBy { it.name == pokemonDetailsName }

    val multipleChoices = groupedResults.getOrElse(false) { emptyList() }.map {
        it.name.capitalize(
            Locale.current
        )
    }.subList(0, 3) + groupedResults.getOrElse(true) { emptyList() }.map {
        it.name.capitalize(
            Locale.current
        )
    }

    return multipleChoices.shuffled(Random)
}


