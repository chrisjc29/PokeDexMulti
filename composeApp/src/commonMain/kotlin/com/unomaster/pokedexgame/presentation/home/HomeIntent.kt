package com.unomaster.pokedexgame.presentation.home

sealed interface HomeIntent {
    // The screen has come into view — on first launch, and again every time a run ends and the
    // player lands back here. It is an intent rather than an init block because the ViewModel
    // survives the trip to the game (NavDisplay keeps the entry's ViewModelStore) while the
    // composable does not, so init would read the best streak once and then show a stale number for
    // the rest of the session.
    data object Shown : HomeIntent
    data object StartGameClicked : HomeIntent
    data object SettingsClicked : HomeIntent
}
