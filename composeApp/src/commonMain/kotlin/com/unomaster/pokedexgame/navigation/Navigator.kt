package com.unomaster.pokedexgame.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

// Owns the back stack and the rules for moving around it. Lives in the Koin graph rather than in
// composition, so it survives configuration changes and is reachable from ViewModels.
class Navigator(
    startDestination: AppRoute,
) : AppNavigator {

    // TRAP: typed as Any, not AppRoute. koinEntryProvider() builds an (Any) -> NavEntry<Any>
    // provider; a narrower list makes NavDisplay infer a narrower type and the provider stops
    // type-checking. The AppNavigator surface above is typed, so callers keep route safety — the
    // looseness is confined to this one field.
    val backStack: SnapshotStateList<Any> = mutableStateListOf(startDestination)

    // Selecting a tab replaces the root rather than piling destinations up, so back from a tab
    // doesn't walk through every tab the user visited.
    override fun navigate(route: AppRoute) {
        if (route.isTopLevel) {
            backStack.clear()
        }
        backStack.add(route)
    }

    // TRAP: refuses to pop the start destination. A double tap would otherwise empty the stack and
    // leave NavDisplay with nothing to render — a blank screen the user can't get out of.
    override fun goBack() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
