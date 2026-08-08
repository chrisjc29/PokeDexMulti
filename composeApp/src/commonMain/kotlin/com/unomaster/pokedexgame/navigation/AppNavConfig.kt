package com.unomaster.pokedexgame.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

// One place that teaches serialization about every route, so back-stack save/restore works on
// every platform including iOS — Nav3 falls back to reflection-based serialization otherwise, and
// reflection does not exist on Kotlin/Native. Add new sealed route hierarchies here as the app
// grows.
//
// This app keeps the back stack in the Koin-owned Navigator, which survives configuration changes
// but NOT process death: Android may restore a backgrounded app at Home. That is an accepted trade
// for a three-screen game with no deep stacks — a restarted round is not a lost draft. This config
// is generated so persisting the stack later is a change in one place.
@OptIn(ExperimentalSerializationApi::class)
val appNavConfig: SavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclassesOfSealed<AppRoute>()
        }
    }
}
