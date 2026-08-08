package com.unomaster.pokedexgame.data.repository

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.unomaster.pokedexgame.analytics.Analytics
import com.unomaster.pokedexgame.data.remote.PokemonRemoteSource
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.model.PokemonQuestion
import com.unomaster.pokedexgame.domain.random.RandomSource
import com.unomaster.pokedexgame.domain.repository.PokemonRepository

class PokemonRepositoryImpl(
    private val remoteSource: PokemonRemoteSource,
    private val randomSource: RandomSource,
    private val analytics: Analytics,
) : PokemonRepository {

    // bind() short-circuits: on a Left neither the detail call nor the analytics call below runs,
    // and the Left is returned unchanged. Note what that removes — there is no
    // `if (result.isFailure) return` to forget between the two requests.
    override suspend fun getQuestion(pageUrl: String?): Either<DomainError, PokemonQuestion> =
        either {
            val page = remoteSource.fetchPage(pageUrl).bind()
            ensure(page.results.size >= CHOICE_COUNT) { DomainError.EmptyResponse }

            val answerEntry = page.results[randomSource.nextInt(page.results.size)]
            val detail = remoteSource.fetchDetail(answerEntry.url).bind()

            val decoyNames = randomSource
                .shuffled(page.results.filterNot { it.name == answerEntry.name })
                .take(CHOICE_COUNT - 1)
                .map { it.name.toDisplayName() }
            val answerName = detail.name.toDisplayName()

            analytics.logEvent(
                name = "pokemon_question_loaded",
                params = mapOf("pokemon_id" to detail.id.toString()),
            )

            // Map DTO -> domain model at the boundary.
            PokemonQuestion(
                answerName = answerName,
                artworkUrl = detail.sprites.other.officialArtwork.frontDefault,
                choices = randomSource.shuffled(decoyNames + answerName),
                nextPageUrl = page.next,
                pokedexNumber = detail.id,
                types = detail.types.map { it.type.name.toDisplayName() },
            )
        }

    private companion object {
        const val CHOICE_COUNT = 4
    }
}

// The API returns lowercase slugs ("bulbasaur"); the buttons show them title-cased. Deliberately
// locale-independent: the names are proper nouns from a fixed English data set, so a locale-aware
// uppercase would only introduce the Turkish dotless-i problem for no benefit.
private fun String.toDisplayName(): String = replaceFirstChar { it.uppercaseChar() }
