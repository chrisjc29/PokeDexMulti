package com.unomaster.pokedexgame.domain.usecase

import arrow.core.left
import arrow.core.right
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.fake.FakePokemonRepository
import com.unomaster.pokedexgame.fake.pokemonQuestion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetPokemonQuestionUseCaseTest {

    @Test
    fun returnsQuestionFromRepository() = runTest {
        val question = pokemonQuestion()
        val useCase = GetPokemonQuestionUseCase(FakePokemonRepository(question))

        assertEquals(question.right(), useCase(pageUrl = null))
    }

    @Test
    fun propagatesRepositoryFailureUnchanged() = runTest {
        val useCase = GetPokemonQuestionUseCase(
            FakePokemonRepository(failWith = DomainError.NetworkUnavailable()),
        )

        // bind() short-circuited: the Left travels out of either { } untouched.
        assertEquals(DomainError.NetworkUnavailable().left(), useCase(pageUrl = null))
    }

    @Test
    fun rejectsRoundWithWrongNumberOfChoices() = runTest {
        val useCase = GetPokemonQuestionUseCase(
            FakePokemonRepository(pokemonQuestion(choices = listOf("Pikachu", "Eevee"))),
        )

        assertEquals(DomainError.EmptyResponse.left(), useCase(pageUrl = null))
    }

    @Test
    fun rejectsRoundThePlayerCannotWin() = runTest {
        // Four choices, none of them the answer — an unwinnable round is a bad response, not
        // something to render.
        val useCase = GetPokemonQuestionUseCase(
            FakePokemonRepository(
                pokemonQuestion(
                    answerName = "Pikachu",
                    choices = listOf("Bulbasaur", "Charmander", "Squirtle", "Eevee"),
                ),
            ),
        )

        assertEquals(DomainError.EmptyResponse.left(), useCase(pageUrl = null))
    }

    @Test
    fun passesThePageCursorThrough() = runTest {
        val repository = FakePokemonRepository()
        val useCase = GetPokemonQuestionUseCase(repository)

        useCase(pageUrl = null)
        useCase(pageUrl = "https://example.test/pokemon?offset=20")

        assertEquals(2, repository.callCount)
        assertEquals(
            listOf(null, "https://example.test/pokemon?offset=20"),
            repository.requestedPageUrls,
        )
    }
}
