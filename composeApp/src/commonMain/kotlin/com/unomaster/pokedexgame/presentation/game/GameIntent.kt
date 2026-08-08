package com.unomaster.pokedexgame.presentation.game

// Everything the UI can do. Cases are nested in the sealed parent → still one top-level type.
sealed interface GameIntent {
    data class ChoiceSelected(val choice: String) : GameIntent
    data object PlayAgain : GameIntent
    data object Retry : GameIntent
    data object DismissError : GameIntent
    data object BackClicked : GameIntent
}
