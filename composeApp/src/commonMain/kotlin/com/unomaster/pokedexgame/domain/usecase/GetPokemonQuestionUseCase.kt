package com.unomaster.pokedexgame.domain.usecase

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.model.PokemonQuestion
import com.unomaster.pokedexgame.domain.repository.PokemonRepository

// A use case is a single, named action. Keeping it as its own class (rather than calling the
// repository straight from the ViewModel) gives the presentation layer one obvious thing to invoke
// and a clean place to add business rules later.
//
// Inside either { } the code reads top-to-bottom like ordinary Kotlin: bind() unwraps a Right and
// short-circuits the whole block on a Left, and ensure() states a business rule as a precondition.
class GetPokemonQuestionUseCase(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(pageUrl: String?): Either<DomainError, PokemonQuestion> = either {
        val question = repository.getQuestion(pageUrl).bind()
        // Business rules enforced here rather than in the ViewModel: a round the player cannot win,
        // or one with the wrong number of buttons, is a bad response and not something to render.
        ensure(question.choices.size == CHOICE_COUNT) { DomainError.EmptyResponse }
        ensure(question.choices.any(question::isCorrect)) { DomainError.EmptyResponse }
        question
    }

    private companion object {
        const val CHOICE_COUNT = 4
    }
}
