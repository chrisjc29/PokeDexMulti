package com.unomaster.pokedexgame.presentation.home

sealed interface HomeIntent {
    data object StartGameClicked : HomeIntent
    data object SettingsClicked : HomeIntent
}
