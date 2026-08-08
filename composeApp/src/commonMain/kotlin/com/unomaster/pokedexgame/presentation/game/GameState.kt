package com.unomaster.pokedexgame.presentation.game

// Everything the UI can be. All four screen states the game has — loading, error, playing and
// solved — are expressible here, and each one has a @Preview so the screenshot tier covers it.
//
// Nothing here is a colour or a label the design owns: `isSolved` is a boolean and `incorrectChoices`
// is a set of names. Which light comes on and which key sinks is decided at render time from
// LocalAppColors, so restyling the dex never touches this file.
data class GameState(
    val isLoading: Boolean = false,
    val artworkUrl: String? = null,
    val choices: List<String> = emptyList(),
    val answerName: String = "",
    val isSolved: Boolean = false,
    // Wrong guesses stay marked so the player can see what they've already ruled out.
    val incorrectChoices: Set<String> = emptySet(),
    val errorMessage: String? = null,
    // Counted from the first round of this visit, not persisted: it is a session counter, and a
    // "ROUND 214" that survives a reinstall would mean nothing to anybody.
    val roundNumber: Int = 1,
    // Consecutive rounds solved without a wrong guess. The best one ever reached is persisted and
    // shown on Home — see GameViewModel.
    val streak: Int = 0,
    val pokedexNumber: Int? = null,
    val types: List<String> = emptyList(),
    val solveSeconds: Int? = null,
)
