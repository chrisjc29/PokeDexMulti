package com.unomaster.pokedexgame.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.unomaster.pokedexgame.TestApplication
import com.unomaster.pokedexgame.presentation.game.GameContent
import com.unomaster.pokedexgame.presentation.game.GameIntent
import com.unomaster.pokedexgame.presentation.game.GameState
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class GameContentComponentTest {

    private val playingState = GameState(
        choices = listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"),
        answerName = "Pikachu",
    )

    @Test
    fun showsEveryChoice_whileUnsolved() = runComposeUiTest {
        setContent { AppTheme { GameContent(state = playingState, onIntent = {}) } }

        playingState.choices.forEach { onNodeWithText(it).assertIsDisplayed() }
    }

    @Test
    fun tappingAChoice_emitsItsIntent() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent { AppTheme { GameContent(state = playingState, onIntent = { lastIntent = it }) } }

        onNodeWithText("Squirtle").performClick()
        // v2 queues dispatched work; an assertion on a raw variable needs an explicit sync point.
        waitForIdle()

        assertEquals(GameIntent.ChoiceSelected("Squirtle"), lastIntent)
    }

    @Test
    fun solvedRound_revealsTheNameAndOffersAnotherRound() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(isSolved = true),
                    onIntent = { lastIntent = it },
                )
            }
        }

        onNodeWithText("It's Pikachu!").assertIsDisplayed()
        // The choices are gone once the round is over — leaving them would invite a tap that
        // silently does nothing.
        onNodeWithText("Bulbasaur").assertDoesNotExist()

        onNodeWithText("Play again").performClick()
        waitForIdle()
        assertEquals(GameIntent.PlayAgain, lastIntent)
    }

    @Test
    fun errorState_offersRetry() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent {
            AppTheme {
                GameContent(
                    state = GameState(errorMessage = "Network unreachable"),
                    onIntent = { lastIntent = it },
                )
            }
        }

        onNodeWithText("Network unreachable").assertIsDisplayed()
        onNodeWithText("Try again").performClick()
        waitForIdle()

        assertEquals(GameIntent.Retry, lastIntent)
    }
}
