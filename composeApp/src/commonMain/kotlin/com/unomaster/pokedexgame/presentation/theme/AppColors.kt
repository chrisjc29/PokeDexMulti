package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// The colours a design has that Material's ColorScheme has no slot for. Add fields here rather than
// writing Color(0xFF...) at a call site — that is how a design system rots.
//
// The pokeball colours live here rather than in the ColorScheme because they are brand marks, not
// roles: the pokeball is red-over-white whether or not the app is in dark mode, and putting it in
// `primary` would mean restyling the app restyles the pokeball.
@Immutable
data class AppColors(
    val pokeballRed: Color,
    val pokeballShell: Color,
    val pokeballOutline: Color,
    val pokeballCore: Color,
    val silhouette: Color,
    // The Pokedex device. Same reasoning as the pokeball, only more so: these describe a physical
    // object — red casing, green LCD, black keys — so they hold the same values in light and dark.
    // They are written out in both palettes rather than hoisted into a shared constant, so that a
    // future dimmed-at-night device is a value change here and nowhere else.
    val deviceShell: Color,
    val deviceCase: Color,
    val deviceScreen: Color,
    val deviceScreenInk: Color,
    val deviceScreenInkDim: Color,
    val deviceScreenInkFaint: Color,
    // Alerts printed on the LCD. A device colour rather than colorScheme.error because the LCD is
    // the same pale green whatever the system theme is, and the red has to stay legible on it.
    val deviceScreenInkAlert: Color,
    val deviceKeyEdge: Color,
    val deviceKeyPressed: Color,
    val deviceKeyLabel: Color,
    val deviceLens: Color,
    val deviceLensActive: Color,
    val deviceLensOffline: Color,
    // The scan bar sweeping the LCD, and the settings switch thumb. Same hex as ledGreenIdle, but a
    // different job — naming it separately is what lets one move without dragging the other.
    val deviceScan: Color,
    // Each indicator light has three levels: idle (the dex is on, nothing has happened), dark
    // (another light has the floor) and on (bright, with a glow behind it).
    val ledRedIdle: Color,
    val ledRed: Color,
    val ledRedOn: Color,
    val ledAmberIdle: Color,
    val ledAmber: Color,
    val ledAmberOn: Color,
    val ledGreenIdle: Color,
    val ledGreen: Color,
    val ledGreenOn: Color,
    val wrongLabel: Color,
    val revealFill: Color,
)

val LightAppColors = AppColors(
    pokeballRed = Color(0xFFEE1515),
    pokeballShell = Color(0xFFFFFFFF),
    pokeballOutline = Color(0xFF1B1B1B),
    pokeballCore = Color(0xFFC1D4E3),
    silhouette = Color(0xFF2B2F3A),
    deviceShell = Color(0xFFB3141A),
    deviceCase = Color(0xFF8B0F14),
    deviceScreen = Color(0xFFDCE9D5),
    deviceScreenInk = Color(0xFF2B2F3A),
    deviceScreenInkDim = Color(0xFF4A5A44),
    deviceScreenInkFaint = Color(0xFF6B7A64),
    deviceScreenInkAlert = Color(0xFFB00020),
    deviceKeyEdge = Color(0xFF12151D),
    deviceKeyPressed = Color(0xFF191C24),
    deviceKeyLabel = Color(0xFFDCE9D5),
    deviceLens = Color(0xFF7ECBFF),
    deviceLensActive = Color(0xFF9FDBFF),
    deviceLensOffline = Color(0xFF4E7E9E),
    deviceScan = Color(0xFF5BC15B),
    ledRedIdle = Color(0xFFE23B3B),
    ledRed = Color(0xFF7A2222),
    ledRedOn = Color(0xFFFF4D4D),
    ledAmberIdle = Color(0xFFF5C542),
    ledAmber = Color(0xFF8A6B22),
    ledAmberOn = Color(0xFFF5C542),
    ledGreenIdle = Color(0xFF5BC15B),
    ledGreen = Color(0xFF2F5F2F),
    ledGreenOn = Color(0xFF4ADE80),
    wrongLabel = Color(0xFFFF6B6B),
    revealFill = Color(0xFFF5C542),
)

val DarkAppColors = AppColors(
    pokeballRed = Color(0xFFEE1515),
    pokeballShell = Color(0xFFF2F2F2),
    pokeballOutline = Color(0xFF0B0B0B),
    pokeballCore = Color(0xFFC1D4E3),
    silhouette = Color(0xFF05070C),
    deviceShell = Color(0xFFB3141A),
    deviceCase = Color(0xFF8B0F14),
    deviceScreen = Color(0xFFDCE9D5),
    deviceScreenInk = Color(0xFF2B2F3A),
    deviceScreenInkDim = Color(0xFF4A5A44),
    deviceScreenInkFaint = Color(0xFF6B7A64),
    deviceScreenInkAlert = Color(0xFFB00020),
    deviceKeyEdge = Color(0xFF12151D),
    deviceKeyPressed = Color(0xFF191C24),
    deviceKeyLabel = Color(0xFFDCE9D5),
    deviceLens = Color(0xFF7ECBFF),
    deviceLensActive = Color(0xFF9FDBFF),
    deviceLensOffline = Color(0xFF4E7E9E),
    deviceScan = Color(0xFF5BC15B),
    ledRedIdle = Color(0xFFE23B3B),
    ledRed = Color(0xFF7A2222),
    ledRedOn = Color(0xFFFF4D4D),
    ledAmberIdle = Color(0xFFF5C542),
    ledAmber = Color(0xFF8A6B22),
    ledAmberOn = Color(0xFFF5C542),
    ledGreenIdle = Color(0xFF5BC15B),
    ledGreen = Color(0xFF2F5F2F),
    ledGreenOn = Color(0xFF4ADE80),
    wrongLabel = Color(0xFFFF6B6B),
    revealFill = Color(0xFFF5C542),
)

// staticCompositionLocalOf, not compositionLocalOf: the value changes only on a theme switch, which
// recomposes everything anyway, so the cheaper read is the right trade.
val LocalAppColors = staticCompositionLocalOf { LightAppColors }
