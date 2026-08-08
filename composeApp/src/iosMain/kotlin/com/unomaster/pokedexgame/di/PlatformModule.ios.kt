package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.analytics.Analytics
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.analytics.IosAnalytics
import com.unomaster.pokedexgame.analytics.IosCrashReporter
import com.unomaster.pokedexgame.data.local.IosKeyValueStore
import com.unomaster.pokedexgame.data.local.KeyValueStore
import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider
import com.unomaster.pokedexgame.time.IosCurrentTimeProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.datetime.TimeZone
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { Darwin.create() }
    single { IosCurrentTimeProvider() } bind CurrentTimeProvider::class
    single { IosKeyValueStore() } bind KeyValueStore::class
    single { TimeZone.currentSystemDefault() }
    single { IosAnalytics() } bind Analytics::class
    single { IosCrashReporter() } bind CrashReporter::class
}
