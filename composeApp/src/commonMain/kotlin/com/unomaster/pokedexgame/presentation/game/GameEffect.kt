package com.unomaster.pokedexgame.presentation.game

// One-shot events that are genuinely not navigation and not state. A wrong guess buzzes once; it
// does not become a property of the screen, so it cannot live in GameState.
sealed interface GameEffect {
    data object WrongAnswerFeedback : GameEffect
    data class ShowMessage(val text: String) : GameEffect
}
