package com.unomaster.pokedexgame.navigation

import com.unomaster.pokedexgame.presentation.game.GameScreen
import com.unomaster.pokedexgame.presentation.home.HomeScreen
import com.unomaster.pokedexgame.presentation.settings.SettingsScreen
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

// Koin's navigation<Route> { } DSL registers the composable for each route, so the mapping lives in
// the DI graph rather than in a growing `when` inside the nav host.
//
// Navigator is bound to both its own type and AppNavigator: the nav host needs the concrete
// backStack, ViewModels need only the interface.
@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    single { Navigator(startDestination = AppRoute.Home) } binds
        arrayOf(Navigator::class, AppNavigator::class)

    navigation<AppRoute.Home> { HomeScreen() }
    navigation<AppRoute.Game> { GameScreen() }
    navigation<AppRoute.Settings> { SettingsScreen() }
}
