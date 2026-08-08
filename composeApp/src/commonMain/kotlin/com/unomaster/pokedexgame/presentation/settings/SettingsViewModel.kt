package com.unomaster.pokedexgame.presentation.settings

import arrow.core.raise.either
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.data.local.KeyValueStore
import com.unomaster.pokedexgame.navigation.AppNavigator
import com.unomaster.pokedexgame.presentation.common.OperationViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val ANALYTICS_ENABLED_KEY = "analytics_enabled"

class SettingsViewModel(
    private val keyValueStore: KeyValueStore,
    private val navigator: AppNavigator,
    crashReporter: CrashReporter,
) : OperationViewModel(crashReporter) {

    private val mutableState = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = mutableState.asStateFlow()

    init {
        // A failed preference read is survivable — the screen renders the default rather than an
        // error — but it's still reported, so a broken store shows up in crash reporting.
        operation(fallbackMessage = "Could not load settings", onFailure = {}) {
            either {
                // Nothing to bind: bestEffort absorbs the failure and returns the fallback, so this
                // block is always a Right. It still returns Either so every operation() body has
                // the same shape.
                val isEnabled = bestEffort(fallback = true) {
                    keyValueStore.getBoolean(ANALYTICS_ENABLED_KEY, default = true)
                }
                mutableState.update { it.copy(isAnalyticsEnabled = isEnabled) }
            }
        }
    }

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.AnalyticsToggled -> {
                mutableState.update { it.copy(isAnalyticsEnabled = intent.isEnabled) }
                keyValueStore.putBoolean(ANALYTICS_ENABLED_KEY, intent.isEnabled)
            }
            SettingsIntent.BackClicked -> navigator.goBack()
        }
    }
}
