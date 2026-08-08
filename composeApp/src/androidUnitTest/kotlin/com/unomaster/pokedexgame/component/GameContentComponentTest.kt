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
import kotlin.test.assertNull
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

    // The keys are stamped in upper case, but the intent still carries the name as the model spells
    // it. Both halves of that are asserted here, because getting one of them wrong is silent.
    @Test
    fun showsEveryChoice_whileUnsolved() = runComposeUiTest {
        setContent { AppTheme { GameContent(state = playingState, onIntent = {}) } }

        playingState.choices.forEach { onNodeWithText(it.uppercase()).assertIsDisplayed() }
    }

    @Test
    fun tappingAChoice_emitsItsIntent() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent { AppTheme { GameContent(state = playingState, onIntent = { lastIntent = it }) } }

        onNodeWithText("SQUIRTLE").performClick()
        // v2 queues dispatched work; an assertion on a raw variable needs an explicit sync point.
        waitForIdle()

        assertEquals(GameIntent.ChoiceSelected("Squirtle"), lastIntent)
    }

    // A ruled-out key stays legible — the player has earned that information — but it is dead.
    @Test
    fun ruledOutChoice_staysVisibleButEmitsNothing() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(incorrectChoices = setOf("Charmander")),
                    onIntent = { lastIntent = it },
                )
            }
        }

        onNodeWithText("CHARMANDER").assertIsDisplayed()
        onNodeWithText("CHARMANDER").performClick()
        waitForIdle()

        assertNull(lastIntent)
    }

    @Test
    fun wrongGuess_reportsHowManyOptionsAreLeft() = runComposeUiTest {
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(incorrectChoices = setOf("Charmander")),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("NO MATCH · 2 LEFT").assertIsDisplayed()
    }

    @Test
    fun solvedRound_revealsTheDexEntryAndOffersAnotherRound() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(
                        isSolved = true,
                        streak = 4,
                        pokedexNumber = 25,
                        types = listOf("Electric"),
                        solveSeconds = 4,
                    ),
                    onIntent = { lastIntent = it },
                )
            }
        }

        onNodeWithText("PIKACHU").assertIsDisplayed()
        onNodeWithText("ELECTRIC").assertIsDisplayed()
        onNodeWithText("NO. 025").assertIsDisplayed()
        // The choices are gone once the round is over — leaving them would invite a tap that
        // silently does nothing.
        onNodeWithText("BULBASAUR").assertDoesNotExist()

        onNodeWithText("NEXT SPECIMEN").performClick()
        waitForIdle()
        assertEquals(GameIntent.PlayAgain, lastIntent)
    }

    @Test
    fun solvedRound_readsBackTheTimeAndStreak() = runComposeUiTest {
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(isSolved = true, streak = 4, solveSeconds = 4),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("Identified in 4 seconds. Streak now 4 — a wrong guess resets it.")
            .assertIsDisplayed()
    }

    // Solving after a wrong guess is still a solve; the copy just doesn't pretend a run survived.
    @Test
    fun solvedRoundWithABrokenStreak_saysSo() = runComposeUiTest {
        setContent {
            AppTheme {
                GameContent(
                    state = playingState.copy(
                        isSolved = true,
                        incorrectChoices = setOf("Charmander"),
                        streak = 0,
                        solveSeconds = 9,
                    ),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("Identified in 9 seconds. Streak reset — the next clean round starts a new one.")
            .assertIsDisplayed()
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

        onNodeWithText("SIGNAL LOST").assertIsDisplayed()
        onNodeWithText("Network unreachable").assertIsDisplayed()
        onNodeWithText("RETRY").performClick()
        waitForIdle()

        assertEquals(GameIntent.Retry, lastIntent)
    }

    @Test
    fun errorState_offersDismiss() = runComposeUiTest {
        var lastIntent: GameIntent? = null
        setContent {
            AppTheme {
                GameContent(
                    state = GameState(errorMessage = "Network unreachable"),
                    onIntent = { lastIntent = it },
                )
            }
        }

        onNodeWithText("DISMISS").performClick()
        waitForIdle()

        assertEquals(GameIntent.DismissError, lastIntent)
    }
}
