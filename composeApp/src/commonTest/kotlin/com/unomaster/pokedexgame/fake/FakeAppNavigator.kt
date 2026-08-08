package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.navigation.AppNavigator
import com.unomaster.pokedexgame.navigation.AppRoute

// Records where the ViewModel tried to go, so navigation is asserted without Compose or a back stack.
class FakeAppNavigator : AppNavigator {
    val visitedRoutes = mutableListOf<AppRoute>()
    var goBackCount: Int = 0
        private set

    override fun navigate(route: AppRoute) {
        visitedRoutes += route
    }

    override fun goBack() {
        goBackCount++
    }
}
