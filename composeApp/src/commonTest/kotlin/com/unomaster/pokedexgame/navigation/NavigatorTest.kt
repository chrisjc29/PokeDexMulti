package com.unomaster.pokedexgame.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigatorTest {

    @Test
    fun pushesAndPopsANonTopLevelRoute() {
        val navigator = Navigator(startDestination = AppRoute.Home)

        navigator.navigate(AppRoute.Game)
        assertEquals(listOf(AppRoute.Home, AppRoute.Game), navigator.backStack.toList())

        navigator.goBack()
        assertEquals(listOf<Any>(AppRoute.Home), navigator.backStack.toList())
    }

    // The guard exists because a double back on the start destination would otherwise empty the
    // stack and leave NavDisplay with nothing to render — a blank screen with no way out.
    @Test
    fun refusesToPopTheLastEntry() {
        val navigator = Navigator(startDestination = AppRoute.Home)

        navigator.goBack()
        navigator.goBack()

        assertEquals(listOf<Any>(AppRoute.Home), navigator.backStack.toList())
    }

    @Test
    fun aTopLevelRouteReplacesTheStackRatherThanPilingOntoIt() {
        val navigator = Navigator(startDestination = AppRoute.Home)
        navigator.navigate(AppRoute.Game)
        navigator.navigate(AppRoute.Settings)

        navigator.navigate(AppRoute.Home)

        assertEquals(listOf<Any>(AppRoute.Home), navigator.backStack.toList())
    }
}
