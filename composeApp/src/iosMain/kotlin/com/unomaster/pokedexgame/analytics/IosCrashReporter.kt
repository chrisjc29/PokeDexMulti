package com.unomaster.pokedexgame.analytics

class IosCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable) {
        IosFirebaseBridges.crashReporter?.recordException(
            message = throwable.message ?: throwable::class.simpleName.orEmpty(),
            stackTrace = throwable.stackTraceToString(),
        )
    }

    override fun setKey(key: String, value: String) {
        IosFirebaseBridges.crashReporter?.setKey(key, value)
    }

    override fun log(message: String) {
        IosFirebaseBridges.crashReporter?.log(message)
    }
}
