package com.unomaster.pokedexgame.navigation

// The one place navigation happens.
//
// One verb, not several. How a destination is reached — pushed onto the stack, or replacing it
// because it's a tab — is a rule about that destination, so it lives in Navigator rather than being
// re-decided at every call site.
interface AppNavigator {
    fun navigate(route: AppRoute)

    fun goBack()
}
