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
    val success: Color,
    val warning: Color,
    val onWarning: Color,
    val divider: Color,
    val scrim: Color,
    val pokeballRed: Color,
    val pokeballShell: Color,
    val pokeballOutline: Color,
    val pokeballCore: Color,
    val silhouette: Color,
)

val LightAppColors = AppColors(
    success = Color(0xFF16A34A),
    warning = Color(0xFFF59E0B),
    onWarning = Color(0xFF1F2937),
    divider = Color(0xFFE1E4ED),
    scrim = Color(0x99000000),
    pokeballRed = Color(0xFFEE1515),
    pokeballShell = Color(0xFFFFFFFF),
    pokeballOutline = Color(0xFF1B1B1B),
    pokeballCore = Color(0xFFC1D4E3),
    silhouette = Color(0xFF2B2F3A),
)

val DarkAppColors = AppColors(
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    onWarning = Color(0xFF1F2937),
    divider = Color(0xFF2C3140),
    scrim = Color(0xB3000000),
    pokeballRed = Color(0xFFEE1515),
    pokeballShell = Color(0xFFF2F2F2),
    pokeballOutline = Color(0xFF0B0B0B),
    pokeballCore = Color(0xFFC1D4E3),
    silhouette = Color(0xFF05070C),
)

// staticCompositionLocalOf, not compositionLocalOf: the value changes only on a theme switch, which
// recomposes everything anyway, so the cheaper read is the right trade.
val LocalAppColors = staticCompositionLocalOf { LightAppColors }
