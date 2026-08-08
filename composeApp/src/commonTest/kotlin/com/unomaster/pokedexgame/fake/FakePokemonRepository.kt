package com.unomaster.pokedexgame.fake

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.model.PokemonQuestion
import com.unomaster.pokedexgame.domain.repository.PokemonRepository

// failWith lets a test drive the error path without a separate failing fake. It's a DomainError
// now, not a Throwable, so a test can only inject a failure the production code actually models —
// you cannot write a test for an error case that cannot happen.
class FakePokemonRepository(
    private val question: PokemonQuestion = pokemonQuestion(),
    private val failWith: DomainError? = null,
) : PokemonRepository {

    var callCount: Int = 0
        private set

    val requestedPageUrls = mutableListOf<String?>()

    override suspend fun getQuestion(pageUrl: String?): Either<DomainError, PokemonQuestion> {
        callCount++
        requestedPageUrls += pageUrl
        return failWith?.left() ?: question.right()
    }
}
