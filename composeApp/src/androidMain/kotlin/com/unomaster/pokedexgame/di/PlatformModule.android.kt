package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.analytics.analyticsModule
import com.unomaster.pokedexgame.data.local.AndroidKeyValueStore
import com.unomaster.pokedexgame.data.local.KeyValueStore
import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider
import com.unomaster.pokedexgame.time.AndroidCurrentTimeProvider
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.datetime.TimeZone
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { AndroidCurrentTimeProvider() } bind CurrentTimeProvider::class
    single { AndroidKeyValueStore(androidContext()) } bind KeyValueStore::class
    // Bound rather than read inline, so tests can substitute a fixed zone. A formatter that calls
    // TimeZone.currentSystemDefault() internally passes locally and fails in CI.
    single { TimeZone.currentSystemDefault() }
    // analyticsModule is defined in whichever variant source set is compiled — androidFirebase
    // (real Firebase) or androidNoFirebase (logcat no-op). Same name either way, so this line is
    // identical regardless of the firebase.enabled flag.
    includes(analyticsModule)
}
