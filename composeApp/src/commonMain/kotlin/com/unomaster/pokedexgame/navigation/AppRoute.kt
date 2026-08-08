package com.unomaster.pokedexgame.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// The whole app's navigation surface, in one place. Add a case per screen; put args as properties.
@Serializable
sealed interface AppRoute : NavKey {

    // Top-level destinations are siblings (bottom-nav tabs), not a stack.
    val isTopLevel: Boolean get() = false

    @Serializable
    data object Home : AppRoute {
        override val isTopLevel: Boolean get() = true
    }

    // No route argument: the API page cursor that drives "play again" is state the GameViewModel
    // owns, not something the user navigates to. A route arg would put it in the back stack and
    // make going back mean "replay that exact page", which is not what back means here.
    @Serializable
    data object Game : AppRoute

    @Serializable
    data object Settings : AppRoute
}
