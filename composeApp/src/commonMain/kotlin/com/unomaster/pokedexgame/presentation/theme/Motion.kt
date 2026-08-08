package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

// Durations and easings live here so unrelated animations agree with each other. A screen that picks
// its own 300ms tween is how an app ends up feeling inconsistent for reasons nobody can name.
object Motion {
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400

    // Material 3's standard easing curves.
    val EasingStandard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val EasingAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
}
