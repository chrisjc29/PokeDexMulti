package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.analytics.Analytics
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.data.local.KeyValueStore
import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider
import com.unomaster.pokedexgame.navigation.AppNavigator
import io.ktor.client.engine.HttpClientEngine
import org.koin.test.verify.verify
import kotlin.test.Test

// In androidUnitTest rather than commonTest on purpose: Koin's verify() walks constructors by
// reflection, so it only exists on the JVM. Putting it in commonTest fails to compile for iOS.
class AppModuleTest {

    @Test
    fun graphResolvesEveryDeclaredDefinition() {
        // verify() checks each definition's constructor dependencies are declared. extraTypes lists
        // what platformModule and navigationModule supply, since neither is on this test's classpath
        // in common code.
        appModule.verify(
            extraTypes = listOf(
                HttpClientEngine::class,
                Analytics::class,
                CrashReporter::class,
                KeyValueStore::class,
                CurrentTimeProvider::class,
                AppNavigator::class,
            ),
        )
    }
}
