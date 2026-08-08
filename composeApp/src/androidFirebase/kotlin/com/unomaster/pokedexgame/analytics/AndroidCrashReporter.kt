package com.unomaster.pokedexgame.analytics

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics

class AndroidCrashReporter : CrashReporter {
    private val crashlytics = Firebase.crashlytics

    override fun recordException(throwable: Throwable) = crashlytics.recordException(throwable)
    override fun setKey(key: String, value: String) = crashlytics.setCustomKey(key, value)
    override fun log(message: String) = crashlytics.log(message)
}
