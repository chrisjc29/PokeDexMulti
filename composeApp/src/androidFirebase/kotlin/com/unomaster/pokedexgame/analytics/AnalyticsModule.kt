package com.unomaster.pokedexgame.analytics

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// platformModule (androidMain) does `includes(analyticsModule)`.
val analyticsModule: Module = module {
    single { AndroidAnalytics() } bind Analytics::class
    single { AndroidCrashReporter() } bind CrashReporter::class
}
