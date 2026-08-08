package com.unomaster.pokedexgame.presentation.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.DeviceKey
import com.unomaster.pokedexgame.presentation.common.DeviceLensState
import com.unomaster.pokedexgame.presentation.common.DeviceLights
import com.unomaster.pokedexgame.presentation.common.DeviceOutlinedKey
import com.unomaster.pokedexgame.presentation.common.DevicePrimaryKey
import com.unomaster.pokedexgame.presentation.common.DeviceScaffold
import com.unomaster.pokedexgame.presentation.common.DeviceScreenPanel
import com.unomaster.pokedexgame.presentation.common.Pokeball
import com.unomaster.pokedexgame.presentation.common.PokemonArtwork
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.DexTypography
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors
import com.unomaster.pokedexgame.presentation.theme.Motion
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

// The screen is three different-looking devices depending on what the dex is doing, so the split is
// by state rather than by one scaffold with five conditional blocks inside it: loading and error run
// the compact casing, a live round runs the full one.
@Composable
fun GameContent(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    when {
        state.isLoading -> LoadingContent(
            modifier = modifier,
            snackbarHostState = snackbarHostState,
            onNavigateBack = { onIntent(GameIntent.BackClicked) },
        )

        state.errorMessage != null -> ErrorContent(
            message = state.errorMessage,
            onIntent = onIntent,
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )

        else -> RoundContent(
            state = state,
            onIntent = onIntent,
            modifier = modifier,
            snackbarHostState = snackbarHostState,
        )
    }
}

@Composable
private fun LoadingContent(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    onNavigateBack: () -> Unit,
) {
    val colors = LocalAppColors.current

    DeviceScaffold(
        modifier = modifier,
        compact = true,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    ) {
        DeviceScreenPanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
                modifier = Modifier.padding(Dimens.DeviceScreenPadding),
            ) {
                SpinningPokeball()
                Text(
                    text = "LOADING SPECIMEN…",
                    style = DexTypography.LoadingLabel,
                    color = colors.deviceScreenInkDim,
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
) {
    val colors = LocalAppColors.current

    DeviceScaffold(
        modifier = modifier,
        compact = true,
        // A dead lens and one red light: the dex says it is offline before a word is read.
        lens = DeviceLensState.Offline,
        lights = DeviceLights.Alert,
        caseGap = Dimens.DeviceCaseGapCompact,
        onNavigateBack = { onIntent(GameIntent.BackClicked) },
        snackbarHostState = snackbarHostState,
    ) {
        DeviceScreenPanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
                modifier = Modifier.padding(Dimens.DeviceScreenPadding),
            ) {
                Text(
                    text = "SIGNAL LOST",
                    style = DexTypography.ChromeTitle,
                    color = colors.deviceScreenInkAlert,
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.deviceScreenInkDim,
                    textAlign = TextAlign.Center,
                )
            }
        }

        DevicePrimaryKey(
            label = "RETRY",
            onClick = { onIntent(GameIntent.Retry) },
            height = Dimens.DeviceRetryKeyHeight,
        )
        DeviceOutlinedKey(
            label = "DISMISS",
            onClick = { onIntent(GameIntent.DismissError) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RoundContent(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
) {
    DeviceScaffold(
        modifier = modifier,
        // The chrome is the scoreboard: which round you are on until you solve it, then which
        // Pokemon you just catalogued.
        metaLabel = if (state.isSolved && state.pokedexNumber != null) {
            "NO. ${state.pokedexNumber.toString().padStart(DEX_NUMBER_DIGITS, '0')}"
        } else {
            "ROUND ${state.roundNumber.toString().padStart(ROUND_DIGITS, '0')}"
        },
        lens = if (state.isSolved) DeviceLensState.Active else DeviceLensState.Idle,
        lights = when {
            state.isSolved -> DeviceLights.Confirmed
            state.incorrectChoices.isNotEmpty() -> DeviceLights.Alert
            else -> DeviceLights.Idle
        },
        onNavigateBack = { onIntent(GameIntent.BackClicked) },
        snackbarHostState = snackbarHostState,
    ) {
        // Nothing here needs to scroll at 360x740, but a large font scale or a short device would
        // otherwise clip the key grid off the bottom of the casing.
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.DeviceCaseGap),
        ) {
            if (state.isSolved) {
                SolvedScreen(state = state)
                DexEntry(state = state)
            } else {
                PlayingScreen(state = state)
                ChoiceLabels(state = state)
                ChoiceGrid(state = state, onIntent = onIntent)
            }
        }

        if (state.isSolved) {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.DeviceKeyGap)) {
                DevicePrimaryKey(
                    label = "NEXT SPECIMEN",
                    onClick = { onIntent(GameIntent.PlayAgain) },
                    modifier = Modifier.weight(1f),
                    height = Dimens.DeviceSolvedKeyHeight,
                )
                DeviceOutlinedKey(
                    label = "END",
                    onClick = { onIntent(GameIntent.BackClicked) },
                    height = Dimens.DeviceSolvedKeyHeight,
                    textStyle = DexTypography.SecondaryKeyLabel,
                )
            }
        }
    }
}

@Composable
private fun PlayingScreen(state: GameState) {
    val colors = LocalAppColors.current
    val wrongGuesses = state.incorrectChoices.size
    val shake = remember { Animatable(0f) }

    // Keyed on the count, so the LCD jumps once per wrong guess and not again on recomposition.
    LaunchedEffect(wrongGuesses) {
        if (wrongGuesses > 0) {
            shake.snapTo(0f)
            shake.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = Motion.DurationShake
                    -SHAKE_UNITS at Motion.DurationShake / 5
                    SHAKE_UNITS at Motion.DurationShake * 2 / 5
                    -SHAKE_UNITS * SHAKE_DECAY at Motion.DurationShake * 3 / 5
                    0f at Motion.DurationShake
                },
            )
        }
    }

    DeviceScreenPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimens.GameScreenHeight)
            .offset(x = Dimens.DeviceShakeOffset * shake.value),
        // The sweep stops the moment a guess lands wrong: a dex still scanning after it has told
        // you "NO MATCH" would be saying two things at once.
        scanning = wrongGuesses == 0,
    ) {
        PokemonArtwork(
            artworkUrl = state.artworkUrl,
            isRevealed = false,
            size = Dimens.SilhouetteSize,
        )
        StatusLine(
            text = if (wrongGuesses > 0) {
                "NO MATCH · ${remainingWrongChoices(state)} LEFT"
            } else {
                "SCANNING…"
            },
            color = if (wrongGuesses > 0) {
                colors.deviceScreenInkAlert
            } else {
                colors.deviceScreenInkDim
            },
        )
    }
}

@Composable
private fun SolvedScreen(state: GameState) {
    val colors = LocalAppColors.current

    DeviceScreenPanel(
        modifier = Modifier.fillMaxWidth().height(Dimens.SolvedScreenHeight),
        glow = true,
    ) {
        PokemonArtwork(
            artworkUrl = state.artworkUrl,
            isRevealed = true,
            size = Dimens.RevealedArtworkSize,
        )
        StatusLine(text = "MATCH CONFIRMED", color = colors.ledGreen)
    }
}

// The dex entry: a second LCD panel below the artwork, which is what turns a "you win" screen into a
// record of something catalogued.
@Composable
private fun DexEntry(state: GameState) {
    val colors = LocalAppColors.current

    DeviceScreenPanel(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Dimens.DeviceCasePadding,
                    vertical = Dimens.DeviceCaseGap,
                ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingExtraSmall),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = state.answerName.uppercase(),
                    style = DexTypography.Heading,
                    color = colors.deviceScreenInk,
                )
                // Only shown when the API actually gave us one — no placeholder type.
                state.types.firstOrNull()?.let { type ->
                    Text(
                        text = type.uppercase(),
                        style = DexTypography.Counter,
                        color = colors.deviceScreenInkFaint,
                    )
                }
            }
            Text(
                text = solveSummary(state),
                style = DexTypography.EntryBody,
                color = colors.deviceScreenInkDim,
            )
        }
    }
}

// Built from what the round actually knows. A missing solve time drops that sentence rather than
// printing "Identified in 0 seconds", and a streak the player just lost says so instead of
// congratulating them on nothing.
private fun solveSummary(state: GameState): String {
    val identified = state.solveSeconds?.let { seconds ->
        val unit = if (seconds == 1) "second" else "seconds"
        "Identified in $seconds $unit. "
    }.orEmpty()

    val streak = if (state.streak > 0) {
        "Streak now ${state.streak} — a wrong guess resets it."
    } else {
        "Streak reset — the next clean round starts a new one."
    }

    return identified + streak
}

@Composable
private fun ChoiceLabels(state: GameState) {
    val colors = LocalAppColors.current
    val hasWrongGuess = state.incorrectChoices.isNotEmpty()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "IDENTIFY THE SPECIMEN",
            style = DexTypography.SectionLabel,
            color = Color.White.copy(alpha = CASE_LABEL_ALPHA),
        )
        Text(
            text = "STREAK ${state.streak}",
            style = DexTypography.Counter,
            // A live streak is lit amber; a broken one goes grey rather than disappearing, so the
            // player can see it was there.
            color = if (hasWrongGuess) {
                Color.White.copy(alpha = CASE_LABEL_MUTED_ALPHA)
            } else {
                colors.ledAmberOn
            },
        )
    }
}

@Composable
private fun ChoiceGrid(
    state: GameState,
    onIntent: (GameIntent) -> Unit,
) {
    // A two-column grid built from Rows rather than LazyVerticalGrid: there are exactly four
    // items, so lazy layout would add scrolling machinery and measurement rules for nothing.
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.DeviceKeyGap)) {
        state.choices.chunked(CHOICES_PER_ROW).forEach { rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.DeviceKeyGap),
            ) {
                rowChoices.forEach { choice ->
                    val isRuledOut = choice in state.incorrectChoices
                    DeviceKey(
                        // Uppercased for display only: the intent carries the name as the model
                        // spells it, so the ViewModel never has to undo a presentation decision.
                        label = choice.uppercase(),
                        onClick = { onIntent(GameIntent.ChoiceSelected(choice)) },
                        modifier = Modifier.weight(1f),
                        pressed = isRuledOut,
                        // A key that has already been ruled out is dead, not merely restyled.
                        enabled = !isRuledOut,
                        strikeThrough = isRuledOut,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.StatusLine(
    text: String,
    color: Color,
) {
    Text(
        text = text,
        style = DexTypography.StatusLine,
        color = color,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(
                top = Dimens.DeviceStatusInsetTop,
                start = Dimens.DeviceStatusInsetStart,
            ),
    )
}

@Composable
private fun SpinningPokeball() {
    val transition = rememberInfiniteTransition(label = "loading")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(Motion.DurationSpin, easing = LinearEasing),
        ),
        label = "spin",
    )

    Pokeball(
        size = Dimens.LoadingPokeballSize,
        modifier = Modifier.rotate(angle),
    )
}

// How many wrong keys are still live. Four choices with one ruled out leaves three keys, but only
// two of them can be wrong again — that is the number worth telling the player.
private fun remainingWrongChoices(state: GameState): Int =
    (state.choices.size - state.incorrectChoices.size - 1).coerceAtLeast(0)

private const val CHOICES_PER_ROW = 2
private const val ROUND_DIGITS = 2
private const val DEX_NUMBER_DIGITS = 3
private const val CASE_LABEL_ALPHA = 0.75f
private const val CASE_LABEL_MUTED_ALPHA = 0.5f
private const val SHAKE_UNITS = 1f
private const val SHAKE_DECAY = 0.6f

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
                roundNumber = 4,
                streak = 3,
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
                roundNumber = 4,
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
                roundNumber = 4,
                streak = 4,
                pokedexNumber = 25,
                types = listOf("Electric"),
                solveSeconds = 4,
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
