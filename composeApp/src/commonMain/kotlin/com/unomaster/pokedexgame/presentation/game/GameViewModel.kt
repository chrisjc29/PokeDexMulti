package com.unomaster.pokedexgame.presentation.game

import arrow.core.raise.either
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.data.local.BestStreakStore
import com.unomaster.pokedexgame.domain.model.PokemonQuestion
import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.navigation.AppNavigator
import com.unomaster.pokedexgame.presentation.common.OperationViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class GameViewModel(
    private val getPokemonQuestion: GetPokemonQuestionUseCase,
    private val navigator: AppNavigator,
    private val bestStreakStore: BestStreakStore,
    private val currentTime: CurrentTimeProvider,
    crashReporter: CrashReporter,
) : OperationViewModel(crashReporter) {

    private val mutableState = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = mutableState.asStateFlow()

    // One-shot effects use a Channel so they fire exactly once, unlike state which re-emits.
    private val effectChannel = Channel<GameEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    // Not UI state — the player never sees it — so it stays out of GameState. It's the API cursor
    // that makes "play again" pull a different set of Pokemon instead of re-rolling the same page.
    private var nextPageUrl: String? = null

    // The current round's rules, kept as the domain model rather than re-derived from GameState:
    // isCorrect() belongs to PokemonQuestion, and copying it into the UI state would duplicate it.
    private var question: PokemonQuestion? = null

    // When the current round's artwork appeared. Not in GameState for the same reason: it is an
    // input to the solve time, not something the screen renders.
    private var roundStartedAt: Long = 0

    // Loading on init rather than from a LaunchedEffect in the screen: the ViewModel is scoped to
    // the nav entry, so this runs once per visit instead of on every recomposition.
    init {
        loadQuestion(pageUrl = null, roundNumber = 1, streak = 0)
    }

    // Single entry point for the UI: send an intent, the ViewModel decides what to do.
    fun onIntent(intent: GameIntent) {
        val current = mutableState.value
        when (intent) {
            is GameIntent.ChoiceSelected -> selectChoice(intent.choice)

            // A new specimen is a new round; a retry is the same round having another go at
            // loading, so only one of them advances the counter.
            GameIntent.PlayAgain -> loadQuestion(
                pageUrl = nextPageUrl,
                roundNumber = current.roundNumber + 1,
                streak = current.streak,
            )

            GameIntent.Retry -> loadQuestion(
                pageUrl = nextPageUrl,
                roundNumber = current.roundNumber,
                streak = current.streak,
            )

            GameIntent.DismissError -> mutableState.update { it.copy(errorMessage = null) }
            GameIntent.BackClicked -> navigator.goBack()
        }
    }

    private fun selectChoice(choice: String) {
        val currentQuestion = question ?: return
        // A solved round ignores further taps: without this, tapping a wrong name after winning
        // would mark the revealed answer as incorrect.
        if (mutableState.value.isSolved) return

        if (currentQuestion.isCorrect(choice)) {
            solve()
        } else {
            // The streak resets the moment a guess is wrong, not at the end of the round: the
            // player should see the cost immediately, next to the key that cost it.
            mutableState.update {
                it.copy(incorrectChoices = it.incorrectChoices + choice, streak = 0)
            }
            effectChannel.trySend(GameEffect.WrongAnswerFeedback)
        }
    }

    private fun solve() {
        val current = mutableState.value
        // Only a first-and-only correct guess extends the streak. A round already spoiled by a
        // wrong guess still counts as solved — it just doesn't count towards the run.
        val streak = if (current.incorrectChoices.isEmpty()) current.streak + 1 else 0
        val solveSeconds = (currentTime.currentEpochSeconds() - roundStartedAt)
            .toInt()
            .coerceAtLeast(0)

        // Outside the update block on purpose: update takes a lambda it may re-run, and a persisted
        // write is not something to run twice.
        bestStreakStore.record(streak)

        mutableState.update {
            it.copy(isSolved = true, streak = streak, solveSeconds = solveSeconds)
        }
    }

    private fun loadQuestion(pageUrl: String?, roundNumber: Int, streak: Int) {
        // A fresh GameState rather than a copy: everything about the previous round — the ruled-out
        // names, the reveal, the error — has to go, and listing what to clear is how one gets left
        // behind. The two counters that outlive a round are passed back in explicitly.
        mutableState.update {
            GameState(isLoading = true, roundNumber = roundNumber, streak = streak)
        }
        operation(
            fallbackMessage = "Could not load a Pokemon",
            onFailure = { message ->
                mutableState.update { it.copy(isLoading = false, errorMessage = message) }
            },
        ) {
            either {
                // bind() short-circuits on a Left, so nothing below runs on failure. There is no
                // success/failure branch here to get wrong.
                val loaded = getPokemonQuestion(pageUrl).bind()
                question = loaded
                nextPageUrl = loaded.nextPageUrl
                roundStartedAt = currentTime.currentEpochSeconds()
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        artworkUrl = loaded.artworkUrl,
                        choices = loaded.choices,
                        answerName = loaded.answerName,
                        pokedexNumber = loaded.pokedexNumber,
                        types = loaded.types,
                    )
                }
            }
        }
    }
}
