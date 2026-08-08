package com.unomaster.pokedexgame.feature

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.unomaster.pokedexgame.TestApplication
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakePokemonRepository
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import com.unomaster.pokedexgame.presentation.game.GameContent
import com.unomaster.pokedexgame.presentation.game.GameViewModel
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import kotlin.test.Test
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
    ) = GameViewModel(
        getPokemonQuestion = GetPokemonQuestionUseCase(repository),
        navigator = navigator,
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
        onNodeWithText("Pikachu").assertIsDisplayed()
    }

    @Test
    fun tappingTheRightName_revealsThePokemon() = runComposeUiTest {
        val gameViewModel = viewModel()
        setContent {
            val state by gameViewModel.state.collectAsState()
            AppTheme { GameContent(state = state, onIntent = gameViewModel::onIntent) }
        }
        waitForIdle()

        onNodeWithText("Pikachu").performClick()
        waitForIdle()

        onNodeWithText("It's Pikachu!").assertIsDisplayed()
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
