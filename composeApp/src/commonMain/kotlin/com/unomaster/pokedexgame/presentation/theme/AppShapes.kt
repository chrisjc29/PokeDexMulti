package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

// Named radii for the shapes Material's five slots don't cover. Reference AppRadius.Pill rather than
// writing RoundedCornerShape(50) inline, so a design change lands in one place.
object AppRadius {
    val Pill = RoundedCornerShape(percent = 50)
    val Card = RoundedCornerShape(12.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
