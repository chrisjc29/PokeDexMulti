package com.unomaster.pokedexgame.data.local

// The only number the game keeps between sessions: the longest run of clean solves the player has
// ever reached. Home reads it, the game writes it.
//
// KeyValueStore speaks String and Boolean. Rather than widen that seam — and every platform actual
// behind it — for one integer, the encoding lives here, in the single place that knows this value is
// a number. A stored value that is missing or unparseable reads as 0, which is also what a new
// install should see.
class BestStreakStore(
    private val keyValueStore: KeyValueStore,
) {
    fun best(): Int = keyValueStore.getString(BEST_STREAK_KEY)?.toIntOrNull() ?: 0

    /** Keeps the higher of the two, so a short run can never overwrite a long one. */
    fun record(streak: Int) {
        if (streak > best()) {
            keyValueStore.putString(BEST_STREAK_KEY, streak.toString())
        }
    }

    private companion object {
        const val BEST_STREAK_KEY = "best_streak"
    }
}
