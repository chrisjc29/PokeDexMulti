package com.unomaster.pokedexgame.presentation.home

import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import com.unomaster.pokedexgame.navigation.AppRoute
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {

    private fun viewModel(navigator: FakeAppNavigator) =
        HomeViewModel(navigator = navigator, crashReporter = NoopCrashReporter)

    @Test
    fun startGame_navigatesToTheGame() {
        val navigator = FakeAppNavigator()

        viewModel(navigator).onIntent(HomeIntent.StartGameClicked)

        assertEquals(listOf<AppRoute>(AppRoute.Game), navigator.visitedRoutes)
    }

    @Test
    fun settings_navigatesToSettings() {
        val navigator = FakeAppNavigator()

        viewModel(navigator).onIntent(HomeIntent.SettingsClicked)

        assertEquals(listOf<AppRoute>(AppRoute.Settings), navigator.visitedRoutes)
    }
}
