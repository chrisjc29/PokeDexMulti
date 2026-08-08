package com.unomaster.pokedexgame.presentation.game

import com.unomaster.pokedexgame.data.local.BestStreakStore
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakeKeyValueStore
import com.unomaster.pokedexgame.fake.FakePokemonRepository
import com.unomaster.pokedexgame.fake.FixedCurrentTimeProvider
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
        keyValueStore: FakeKeyValueStore = FakeKeyValueStore(),
        currentTime: FixedCurrentTimeProvider = FixedCurrentTimeProvider(),
    ) = GameViewModel(
        getPokemonQuestion = GetPokemonQuestionUseCase(repository),
        navigator = navigator,
        bestStreakStore = BestStreakStore(keyValueStore),
        currentTime = currentTime,
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
    fun loadedRound_carriesTheDexEntryFields() = runTest {
        val gameViewModel = viewModel()

        testScheduler.advanceUntilIdle()

        assertEquals(25, gameViewModel.state.value.pokedexNumber)
        assertEquals(listOf("Electric"), gameViewModel.state.value.types)
    }

    @Test
    fun cleanSolve_extendsTheStreak() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertEquals(1, gameViewModel.state.value.streak)
    }

    @Test
    fun wrongGuess_resetsTheStreakImmediately() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))
        gameViewModel.onIntent(GameIntent.PlayAgain)
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Squirtle"))

        assertEquals(0, gameViewModel.state.value.streak)
    }

    // The distinction the design asks for: a round is still solved after a wrong guess, it just does
    // not count towards the run.
    @Test
    fun solvingAfterAWrongGuess_doesNotExtendTheStreak() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Squirtle"))
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertTrue(gameViewModel.state.value.isSolved)
        assertEquals(0, gameViewModel.state.value.streak)
    }

    @Test
    fun streakSurvivesTheNextRound() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        gameViewModel.onIntent(GameIntent.PlayAgain)
        testScheduler.advanceUntilIdle()
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertEquals(2, gameViewModel.state.value.streak)
    }

    @Test
    fun bestStreakIsPersisted() = runTest {
        val store = FakeKeyValueStore()
        val gameViewModel = viewModel(keyValueStore = store)
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertEquals(1, BestStreakStore(store).best())
    }

    // A short run must not overwrite a long one — the number on Home is a personal best, not a
    // last-seen value.
    @Test
    fun aShorterRun_doesNotLowerTheStoredBest() = runTest {
        val store = FakeKeyValueStore(mapOf("best_streak" to "9"))
        val gameViewModel = viewModel(keyValueStore = store)
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertEquals(9, BestStreakStore(store).best())
    }

    @Test
    fun solveTimeIsMeasuredFromWhenTheRoundLoaded() = runTest {
        val clock = FixedCurrentTimeProvider(epochSeconds = 1_000L)
        val gameViewModel = viewModel(currentTime = clock)
        testScheduler.advanceUntilIdle()

        clock.epochSeconds = 1_004L
        gameViewModel.onIntent(GameIntent.ChoiceSelected("Pikachu"))

        assertEquals(4, gameViewModel.state.value.solveSeconds)
    }

    @Test
    fun playAgain_advancesTheRoundNumber() = runTest {
        val gameViewModel = viewModel()
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.PlayAgain)
        testScheduler.advanceUntilIdle()

        assertEquals(2, gameViewModel.state.value.roundNumber)
    }

    // A retry is the same round having another go at loading, so the counter must not move — a
    // flaky connection should not look like progress.
    @Test
    fun retry_staysOnTheSameRound() = runTest {
        val gameViewModel = viewModel(
            repository = FakePokemonRepository(failWith = DomainError.RateLimited()),
        )
        testScheduler.advanceUntilIdle()

        gameViewModel.onIntent(GameIntent.Retry)
        testScheduler.advanceUntilIdle()

        assertEquals(1, gameViewModel.state.value.roundNumber)
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
