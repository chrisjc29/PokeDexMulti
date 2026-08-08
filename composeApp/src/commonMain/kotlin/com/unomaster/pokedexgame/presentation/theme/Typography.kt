package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The system font by default. If a design ships .ttf/.otf files, drop them into
// composeApp/src/commonMain/composeResources/font/ and set fontFamily here via the Compose
// Resources Font(...) API — one change, and every screen picks it up.
val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
)

// The dex's own voice. Everything that is *device chrome* — status lines, key labels, counters,
// titles etched on the casing — is monospace with letter-spacing, which is what makes it read as
// machine output rather than app text. Prose stays on AppTypography's system font: a paragraph set
// in monospace is slower to read, and the Pokemon description is the one thing here that is prose.
//
// These are not Material roles, so they are not in the Typography above — a Text that wants one asks
// for it by name, the same way it asks AppColors for the LCD green.
object DexTypography {
    private val Mono = FontFamily.Monospace

    /** `POKEDEX GAME` on the home LCD. */
    val Title = TextStyle(
        fontFamily = Mono,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** `PIKACHU` in the dex entry after a solve. */
    val Heading = TextStyle(
        fontFamily = Mono,
        fontSize = 22.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** The label on the casing next to the lens, e.g. `SETTINGS`, and `SIGNAL LOST`. */
    val ChromeTitle = TextStyle(
        fontFamily = Mono,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
    )

    /** Right-hand chrome meta: `ROUND 04`, `NO. 025`, `SETTINGS` on home. */
    val HeaderMeta = TextStyle(
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** `IDENTIFY THE SPECIMEN` and `BEST STREAK · 12`. */
    val SectionLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
    )

    /** `STREAK 3` and the `ELECTRIC` type tag — same size as SectionLabel, tighter tracking. */
    val Counter = TextStyle(
        fontFamily = Mono,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** Printed inside the LCD: `SCANNING…`, `NO MATCH · 2 LEFT`, `MATCH CONFIRMED`. */
    val StatusLine = TextStyle(
        fontFamily = Mono,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** `LOADING SPECIMEN…`. */
    val LoadingLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
    )

    /** A name key in the answer grid. */
    val KeyLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** The one big action key: `START GAME`, `NEXT SPECIMEN`, `RETRY`. */
    val PrimaryKeyLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.5.sp,
    )

    /** The key beside a primary one: `END`. */
    val SecondaryKeyLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** A standalone outlined key: `DISMISS`. */
    val OutlinedKeyLabel = TextStyle(
        fontFamily = Mono,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    /** Prose on the LCD — system font on purpose. The dex entry's description line. */
    val EntryBody = TextStyle(fontSize = 13.sp, lineHeight = 18.sp)
}
