package com.unomaster.pokedexgame.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.koinInject
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun AppNavDisplay() {
    val navigator = koinInject<Navigator>()

    NavDisplay(
        backStack = navigator.backStack,
        // Nav3 1.0.0-alpha05 types onBack as () -> Unit, not (Int) -> Unit — checked against the
        // pinned version rather than assumed. If a later alpha reintroduces the count-aware form,
        // honour the count (`{ count -> repeat(count) { navigator.goBack() } }`): discarding it
        // under-pops on a multi-entry predictive-back gesture.
        onBack = { navigator.goBack() },
        // TRAP — do not omit these. Without them a destination gets no ViewModelStore of its own,
        // so koinViewModel() hands back a fresh ViewModel on every recomposition and all state is
        // discarded: the game would refetch a new Pokemon on every frame. The saveable holder does
        // the same job for rememberSaveable state such as scroll position.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = koinEntryProvider(),
    )
}
