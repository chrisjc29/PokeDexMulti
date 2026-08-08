package com.unomaster.pokedexgame.domain.model

// One round of the game: the artwork to silhouette, the four names to choose between, and the URL
// of the next API page so "play again" pulls a different set of Pokemon.
data class PokemonQuestion(
    val answerName: String,
    val artworkUrl: String,
    val choices: List<String>,
    val nextPageUrl: String?,
) {
    // The rule that decides a round, kept with the data it judges rather than in the ViewModel.
    fun isCorrect(choice: String): Boolean = choice.equals(answerName, ignoreCase = true)
}
