package com.unomaster.pokedexgame.presentation.home

// Home had no state until the dex learned to remember. It has exactly one thing to say now: how far
// the player has ever got. Zero means "never finished a clean round", and the screen leaves the line
// off entirely rather than printing a boast of nothing.
data class HomeState(
    val bestStreak: Int = 0,
)
