package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.analytics.CrashReporter

// An object, not a class: it holds no state and every test wants the same one. Use a
// RecordingCrashReporter instead if a test needs to assert that a failure was reported.
object NoopCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) = Unit
    override fun setKey(key: String, value: String) = Unit
    override fun log(message: String) = Unit
}
