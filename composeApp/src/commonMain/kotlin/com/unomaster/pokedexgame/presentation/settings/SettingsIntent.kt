package com.unomaster.pokedexgame.presentation.settings

sealed interface SettingsIntent {
    data class AnalyticsToggled(val isEnabled: Boolean) : SettingsIntent
    data object BackClicked : SettingsIntent
}
