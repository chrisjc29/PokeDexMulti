package com.unomaster.pokedexgame.feature

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.unomaster.pokedexgame.TestApplication
import com.unomaster.pokedexgame.data.local.BestStreakStore
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakeKeyValueStore
import com.unomaster.pokedexgame.fake.FakePokemonRepository
import com.unomaster.pokedexgame.fake.FixedCurrentTimeProvider
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import com.unomaster.pokedexgame.presentation.game.GameContent
import com.unomaster.pokedexgame.presentation.game.GameViewModel
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Drives the whole screen end to end through the MVI loop: a real ViewModel over fakes, rendering
// the real composable, asserting on what the player would see.
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class GameFeatureTest {

    private fun viewModel(
        repository: FakePokemonRepository = FakePokemonRepository(),
        navigator: FakeAppNavigator = FakeAppNavigator(),
        keyValueStore: FakeKeyValueStore = FakeKeyValueStore(),
    ) = GameViewModel(
        getPokemonQuestion = GetPokemonQuestionUseCase(repository),
        navigator = navigator,
        bestStreakStore = BestStreakStore(keyValueStore),
        currentTime = FixedCurrentTimeProvider(),
        crashReporter = NoopCrashReporter,
    )

    @Test
    fun loadedRound_rendersTheChoicesFromTheUseCase() = runComposeUiTest {
        val gameViewModel = viewModel()
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }

        waitForIdle()
        onNodeWithText("PIKACHU").assertIsDisplayed()
    }

    @Test
    fun tappingTheRightName_revealsThePokemon() = runComposeUiTest {
        val gameViewModel = viewModel()
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }
        waitForIdle()

        onNodeWithText("PIKACHU").performClick()
        waitForIdle()

        // The key grid gives way to the dex entry, and the LCD says the match landed.
        onNodeWithText("MATCH CONFIRMED").assertIsDisplayed()
        onNodeWithText("NEXT SPECIMEN").assertIsDisplayed()
    }

    // The run counter is the reason to keep playing, so it is worth driving through the real loop
    // rather than only asserting on the ViewModel.
    @Test
    fun aCleanSolve_persistsTheBestStreak() = runComposeUiTest {
        val store = FakeKeyValueStore()
        val gameViewModel = viewModel(keyValueStore = store)
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }
        waitForIdle()

        onNodeWithText("PIKACHU").performClick()
        waitForIdle()

        assertEquals(1, BestStreakStore(store).best())
    }

    @Test
    fun aWrongName_marksTheKeyAndKeepsTheRoundOpen() = runComposeUiTest {
        val gameViewModel = viewModel()
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }
        waitForIdle()

        onNodeWithText("SQUIRTLE").performClick()
        waitForIdle()

        onNodeWithText("NO MATCH · 2 LEFT").assertIsDisplayed()
        onNodeWithText("PIKACHU").assertIsDisplayed()
    }

    @Test
    fun failingLoad_rendersUserFacingMessage() = runComposeUiTest {
        val gameViewModel = viewModel(
            repository = FakePokemonRepository(
                failWith = DomainError.Unexpected(IllegalStateException("boom")),
            ),
        )
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }
        waitForIdle()

        // The fallback wording from GameViewModel, not the exception's own message.
        onNodeWithText("Could not load a Pokemon").assertIsDisplayed()
    }
}
