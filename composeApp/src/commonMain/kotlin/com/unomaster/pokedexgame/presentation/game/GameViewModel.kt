package com.unomaster.pokedexgame.presentation.game

import arrow.core.raise.either
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.domain.model.PokemonQuestion
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

    // Loading on init rather than from a LaunchedEffect in the screen: the ViewModel is scoped to
    // the nav entry, so this runs once per visit instead of on every recomposition.
    init {
        loadQuestion(pageUrl = null)
    }

    // Single entry point for the UI: send an intent, the ViewModel decides what to do.
    fun onIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.ChoiceSelected -> selectChoice(intent.choice)
            GameIntent.PlayAgain -> loadQuestion(pageUrl = nextPageUrl)
            GameIntent.Retry -> loadQuestion(pageUrl = nextPageUrl)
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
            mutableState.update { it.copy(isSolved = true) }
        } else {
            mutableState.update { it.copy(incorrectChoices = it.incorrectChoices + choice) }
            effectChannel.trySend(GameEffect.WrongAnswerFeedback)
        }
    }

    private fun loadQuestion(pageUrl: String?) {
        mutableState.update {
            GameState(isLoading = true)
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
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        artworkUrl = loaded.artworkUrl,
                        choices = loaded.choices,
                        answerName = loaded.answerName,
                    )
                }
            }
        }
    }
}
