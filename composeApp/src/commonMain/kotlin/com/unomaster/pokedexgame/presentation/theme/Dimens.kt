package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.ui.unit.dp

// A 4dp-based spacing scale. Screens reference these rather than literal dp values, which is what
// keeps padding consistent as the app grows.
object Dimens {
    val SpacingExtraSmall = 4.dp
    val SpacingSmall = 8.dp
    val SpacingMedium = 16.dp
    val SpacingLarge = 24.dp
    val SpacingExtraLarge = 32.dp

    val ScreenPadding = 16.dp
    val CardPadding = 16.dp

    // 48dp is the floor in both Material 3 and Apple's HIG (44pt). Anything tappable gets at least
    // this, applied with heightIn/sizeIn rather than fixed size, so text can still grow.
    val MinimumTouchTarget = 48.dp

    val IconSmall = 16.dp
    val IconMedium = 24.dp
    val IconLarge = 40.dp

    val DividerThickness = 1.dp
    val Elevation = 2.dp

    // Game-specific sizes: the artwork and the pokeball on the start screen.
    val ArtworkSize = 260.dp
    val PokeballSize = 220.dp
}
