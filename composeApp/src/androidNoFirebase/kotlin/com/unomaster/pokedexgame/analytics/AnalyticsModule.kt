package com.unomaster.pokedexgame.analytics

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// Same name/shape as the firebase variant — platformModule includes it identically.
val analyticsModule: Module = module {
    single { LogcatAnalytics() } bind Analytics::class
    single { LogcatCrashReporter() } bind CrashReporter::class
}
