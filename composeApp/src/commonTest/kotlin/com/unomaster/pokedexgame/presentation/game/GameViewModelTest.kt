package com.unomaster.pokedexgame.presentation.game

import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakePokemonRepository
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import com.unomaster.pokedexgame.fake.pokemonQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameViewModelTest {

    // viewModelScope runs on Dispatchers.Main, which doesn't exist in a plain unit test. Replacing it
    // with a test dispatcher is what makes runTest able to advance the ViewModel's coroutines.
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        repository: FakePokemonRepository = FakePokemonRepository(),
        navigator: FakeAppNavigator = FakeAppNavigator(),
    ) = GameViewModel(
        getPokemonQuestion = GetPokemonQuestionUseCase(repository),
        navigator = navigator,
        crashReporter = NoopCrashReporter,
    )

    @Test
    fun loadsARoundOnInit() = runTest {
        val gameViewModel = viewModel()

        testScheduler.advanceUntilIdle()

        val state = gameViewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("Pikachu", state.answerName)
        assertEquals(listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"), state.choices)
        assertEquals("https://example.test/pikachu.png", state.artworkUrl)
    }

    @Test
    fun correctChoice_solvesTheRound() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertTrue(gameViewModel.state.value.isSolved)
        assertTrue(gameViewModel.state.value.incorrectChoices.isEmpty())
    }

    @Test
    fun wrongChoice_isRuledOutAndTheRoundStaysOpen() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Squirtle"))

        assertFalse(gameViewModel.state.value.isSolved)
        assertEquals(setOf("Squirtle"), gameViewModel.state.value.incorrectChoices)
    }

    @Test
    fun choicesAfterSolving_areIgnored() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Squirtle"))

        assertTrue(gameViewModel.state.value.isSolved)
        assertTrue(gameViewModel.state.value.incorrectChoices.isEmpty())
    }

    @Test
    fun playAgain_requestsTheNextPage() = runTest {
        val repository = FakePokemonRepository(
            pokemonQuestion(nextPageUrl = "https://example.test/pokemon?offset=40"),
        )
        val gameViewModel = viewModel(repository = repository)
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.PlayAgain)
        testScheduler.advanceUntilIdle()

        // First round asks for the default page; the second follows the API's own cursor, so the
        // player doesn't get the same twenty Pokemon every time.
        assertEquals(
            listOf(null, "https://example.test/pokemon?offset=40"),
            repository.requestedPageUrls,
        )
    }

    @Test
    fun playAgain_clearsTheSolvedRound() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        gameViewModel.onIntent(GameIntent.PlayAgain)
        testScheduler.advanceUntilIdle()

        assertFalse(gameViewModel.state.value.isSolved)
    }

    @Test
    fun recognisedFailure_getsItsOwnWording_notTheFallback() = runTest {
        val gameViewModel = viewModel(
            repository = FakePokemonRepository(failWith = DomainError.NetworkUnavailable()),
        )

        testScheduler.advanceUntilIdle()

        assertEquals(
            "Couldn't connect. Check your connection and try again.",
            gameViewModel.state.value.errorMessage,
        )
    }

    @Test
    fun unrecognisedFailure_getsTheScreensFallbackWording() = runTest {
        val gameViewModel = viewModel(
            repository = FakePokemonRepository(
                failWith = DomainError.Unexpected(IllegalStateException("boom")),
            ),
        )

        testScheduler.advanceUntilIdle()

        assertEquals("Could not load a Pokemon", gameViewModel.state.value.errorMessage)
    }

    @Test
    fun dismissError_clearsIt() = runTest {
        val gameViewModel = viewModel(
            repository = FakePokemonRepository(failWith = DomainError.RateLimited()),
        )
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.DismissError)

        assertEquals(null, gameViewModel.state.value.errorMessage)
    }

    @Test
    fun backClicked_popsTheStack() = runTest {
        val navigator = FakeAppNavigator()
        val gameViewModel = viewModel(navigator = navigator)
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.BackClicked)

        assertEquals(1, navigator.goBackCount)
    }
}
