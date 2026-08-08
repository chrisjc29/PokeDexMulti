package com.unomaster.pokedexgame.presentation.theme

// Durations live here so unrelated animations agree with each other. A screen that picks its own
// 300ms tween is how an app ends up feeling inconsistent for reasons nobody can name.
object Motion {
    // The scan sweep and the loading spin are ambient — they say the machine is working — rather
    // than responses to a tap, which is why they run far longer than the reaction to a guess. A
    // 250ms sweep would read as a flicker.
    const val DurationScanSweep = 2600
    const val DurationSpin = 1400

    // Reactions to a guess.
    const val DurationShake = 350
    const val DurationReveal = 300
}
