package com.unomaster.pokedexgame.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Pokedex red and the blue of the device shell, kept in Material's slots so components pick them up
// without a single hardcoded colour at a call site.
private val LightColors = lightColorScheme(
    primary = Color(0xFFCC0000),
    onPrimary = Color.White,
    secondary = Color(0xFF3B4CCA),
    onSecondary = Color.White,
    background = Color(0xFFF5F6FA),
    onBackground = Color(0xFF11131A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF11131A),
    onSurfaceVariant = Color(0xFF5B6172),
    error = Color(0xFFB00020),
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF5A5A),
    onPrimary = Color(0xFF1A0000),
    secondary = Color(0xFF8DA0FF),
    onSecondary = Color(0xFF000A2E),
    background = Color(0xFF101218),
    onBackground = Color(0xFFE6E8F0),
    surface = Color(0xFF1A1D26),
    onSurface = Color(0xFFE6E8F0),
    onSurfaceVariant = Color(0xFF9AA1B4),
    error = Color(0xFFCF6679),
    onError = Color.Black,
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The extras that Material has no slot for travel through a CompositionLocal, because they
    // differ between light and dark. If the app ever drops to a single theme, a plain object is
    // simpler and this provider goes away.
    CompositionLocalProvider(
        LocalAppColors provides if (darkTheme) DarkAppColors else LightAppColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}
