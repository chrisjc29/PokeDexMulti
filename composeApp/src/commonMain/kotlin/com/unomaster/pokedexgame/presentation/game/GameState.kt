package com.unomaster.pokedexgame.presentation.game

// Everything the UI can be. All four screen states the game has — loading, error, playing and
// solved — are expressible here, and each one has a @Preview so the screenshot tier covers it.
data class GameState(
    val isLoading: Boolean = false,
    val artworkUrl: String? = null,
    val choices: List<String> = emptyList(),
    val answerName: String = "",
    val isSolved: Boolean = false,
    // Wrong guesses stay marked so the player can see what they've already ruled out.
    val incorrectChoices: Set<String> = emptySet(),
    val errorMessage: String? = null,
)
