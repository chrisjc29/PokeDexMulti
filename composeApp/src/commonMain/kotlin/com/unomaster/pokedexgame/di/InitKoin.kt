package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.navigation.navigationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

// Keep this function boring. It never calls stopKoin() — under Robolectric a fresh Application is
// built per test against Koin's static container, and the fix for that belongs in the test
// configuration (TestApplication + robolectric.properties), not in the app's startup path.
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(appModule, navigationModule, platformModule)
    }
