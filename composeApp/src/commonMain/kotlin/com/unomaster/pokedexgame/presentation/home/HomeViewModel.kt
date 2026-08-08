package com.unomaster.pokedexgame.presentation.home

import arrow.core.raise.either
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.data.local.BestStreakStore
import com.unomaster.pokedexgame.navigation.AppNavigator
import com.unomaster.pokedexgame.navigation.AppRoute
import com.unomaster.pokedexgame.presentation.common.OperationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Still no HomeEffect: the start screen fires nothing one-shot. It does have state now — the best
// streak the player has ever reached — so HomeState exists and an empty HomeEffect does not.
class HomeViewModel(
    private val navigator: AppNavigator,
    private val bestStreakStore: BestStreakStore,
    crashReporter: CrashReporter,
) : OperationViewModel(crashReporter) {

    private val mutableState = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = mutableState.asStateFlow()

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Shown -> refreshBestStreak()
            HomeIntent.StartGameClicked -> navigator.navigate(AppRoute.Game)
            HomeIntent.SettingsClicked -> navigator.navigate(AppRoute.Settings)
        }
    }

    private fun refreshBestStreak() {
        // A failed read is survivable — the screen simply leaves the line off — but it is still
        // reported, so a broken store shows up in crash reporting rather than as a missing label.
        operation(fallbackMessage = "Could not load your best streak", onFailure = {}) {
            either {
                // Nothing to bind: bestEffort absorbs the failure and returns the fallback, so this
                // block is always a Right. It still returns Either so every operation() body has
                // the same shape.
                val best = bestEffort(fallback = 0) { bestStreakStore.best() }
                mutableState.update { it.copy(bestStreak = best) }
            }
        }
    }
}
