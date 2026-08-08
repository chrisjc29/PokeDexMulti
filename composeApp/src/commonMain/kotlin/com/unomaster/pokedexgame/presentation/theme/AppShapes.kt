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

// The Pokedex device's own shapes. The case corner is deliberately asymmetric — the larger
// bottom-end radius is what stops the red panel reading as a card and starts it reading as moulded
// plastic — so it cannot be expressed by one of Material's symmetric slots.
object DeviceShapes {
    val Case = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomEnd = 26.dp,
        bottomStart = 12.dp,
    )

    // The loading and error screens run a slightly tighter casing, matching their smaller chrome.
    val CaseCompact = RoundedCornerShape(
        topStart = 12.dp,
        topEnd = 12.dp,
        bottomEnd = 24.dp,
        bottomStart = 12.dp,
    )

    val Screen = RoundedCornerShape(8.dp)
    val Key = RoundedCornerShape(10.dp)
    val PrimaryKey = RoundedCornerShape(12.dp)
}
