package com.unomaster.pokedexgame.analytics

// Implemented in Swift using the Firebase iOS SDK; registered at launch.
// Takes strings rather than a Throwable: a Kotlin Throwable means nothing to Crashlytics' iOS API,
// which records an NSError.
interface CrashReporterBridge {
    fun recordException(message: String, stackTrace: String)
    fun setKey(key: String, value: String)
    fun log(message: String)
}
