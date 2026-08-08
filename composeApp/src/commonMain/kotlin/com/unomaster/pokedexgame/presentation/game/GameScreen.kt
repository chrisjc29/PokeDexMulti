package com.unomaster.pokedexgame.presentation.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.PokemonArtwork
import com.unomaster.pokedexgame.presentation.common.ScreenScaffold
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.Dimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                GameEffect.WrongAnswerFeedback ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)

                is GameEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
            }
        }
    }

    GameContent(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun GameContent(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    ScreenScaffold(
        title = "Who's that Pokemon?",
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        onNavigateBack = { onIntent(GameIntent.BackClicked) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()

                state.errorMessage != null -> ErrorState(
                    message = state.errorMessage,
                    onIntent = onIntent,
                )

                else -> RoundState(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onIntent: (GameIntent) -> Unit,
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
    )
    Button(onClick = { onIntent(GameIntent.Retry) }) { Text("Try again") }
    OutlinedButton(onClick = { onIntent(GameIntent.DismissError) }) { Text("Dismiss") }
}

@Composable
private fun RoundState(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
) {
    PokemonArtwork(artworkUrl = state.artworkUrl, isRevealed = state.isSolved)

    if (state.isSolved) {
        Text(
            text = "It's ${state.answerName}!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = { onIntent(GameIntent.PlayAgain) },
            modifier = Modifier.heightIn(min = Dimens.MinimumTouchTarget),
        ) {
            Text("Play again")
        }
    } else {
        // A two-column grid built from Rows rather than LazyVerticalGrid: there are exactly four
        // items, so lazy layout would add scrolling machinery and measurement rules for nothing.
        state.choices.chunked(CHOICES_PER_ROW).forEach { rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
            ) {
                rowChoices.forEach { choice ->
                    ChoiceButton(
                        choice = choice,
                        isRuledOut = choice in state.incorrectChoices,
                        onClick = { onIntent(GameIntent.ChoiceSelected(choice)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoiceButton(
    choice: String,
    isRuledOut: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = Dimens.MinimumTouchTarget),
        // A ruled-out name stays on screen and readable — greying it out to the point of
        // illegibility hides information the player has already earned.
        colors = if (isRuledOut) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
    ) {
        Text(text = choice, maxLines = 1)
    }
}

private const val CHOICES_PER_ROW = 2

// Previews double as the source for Roborazzi's regression screenshots. Each preview = one generated
// screenshot test, so every state the screen can be in gets one. They pass a null artwork URL on
// purpose: the placeholder renders instead, so no golden depends on the network.
@Preview
@Composable
fun GameContentPlayingPreview() {
    AppTheme {
        GameContent(
            state = GameState(
                choices = listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"),
                answerName = "Pikachu",
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
fun GameContentWrongGuessPreview() {
    AppTheme {
        GameContent(
            state = GameState(
                choices = listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"),
                answerName = "Pikachu",
                incorrectChoices = setOf("Charmander"),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
fun GameContentSolvedPreview() {
    AppTheme {
        GameContent(
            state = GameState(
                choices = listOf("Bulbasaur", "Charmander", "Squirtle", "Pikachu"),
                answerName = "Pikachu",
                isSolved = true,
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
fun GameContentLoadingPreview() {
    AppTheme { GameContent(state = GameState(isLoading = true), onIntent = {}) }
}

@Preview
@Composable
fun GameContentErrorPreview() {
    AppTheme {
        GameContent(
            state = GameState(errorMessage = "Couldn't connect. Check your connection and try again."),
            onIntent = {},
        )
    }
}
