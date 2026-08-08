package com.unomaster.pokedexgame.presentation.home

import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.navigation.AppNavigator
import com.unomaster.pokedexgame.navigation.AppRoute
import com.unomaster.pokedexgame.presentation.common.OperationViewModel

// No HomeState and no HomeEffect: the start screen renders nothing that varies and fires nothing
// one-shot. Generating an empty state class so every feature has four files would be ceremony —
// add them the moment the screen actually has state to hold.
//
// It still has a ViewModel, because navigation is a decision and decisions belong here rather than
// in a composable holding an onStartGame callback.
class HomeViewModel(
    private val navigator: AppNavigator,
    crashReporter: CrashReporter,
) : OperationViewModel(crashReporter) {

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.StartGameClicked -> navigator.navigate(AppRoute.Game)
            HomeIntent.SettingsClicked -> navigator.navigate(AppRoute.Settings)
        }
    }
}
