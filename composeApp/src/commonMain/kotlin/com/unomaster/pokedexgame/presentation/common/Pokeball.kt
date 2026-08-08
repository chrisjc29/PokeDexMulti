package com.unomaster.pokedexgame.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors

// Drawn rather than shipped as an asset: it scales to any size without a set of density buckets, and
// it costs nothing at build time. Colours come from AppColors, so the one place to restyle it is the
// token file.
@Composable
fun Pokeball(
    modifier: Modifier = Modifier,
    size: Dp = Dimens.PokeballSize,
) {
    val colors = LocalAppColors.current

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Pokeball" },
    ) {
        val diameter = this.size.minDimension
        val strokeWidth = diameter * OUTLINE_FRACTION

        drawArc(
            color = colors.pokeballShell,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = true,
        )
        drawArc(
            color = colors.pokeballRed,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
        )
        drawArc(
            color = colors.pokeballOutline,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(strokeWidth),
        )
        drawLine(
            color = colors.pokeballOutline,
            start = Offset(x = 0f, y = this.size.height / 2),
            end = Offset(x = this.size.width, y = this.size.height / 2),
            strokeWidth = strokeWidth,
        )
        drawCircle(color = colors.pokeballOutline, radius = diameter * OUTER_BUTTON_FRACTION / 2)
        drawCircle(color = colors.pokeballCore, radius = diameter * INNER_BUTTON_FRACTION / 2)

        val coreDiameter = diameter * CORE_FRACTION
        val coreInset = (diameter - coreDiameter) / 2
        drawArc(
            color = colors.pokeballOutline,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(x = coreInset, y = coreInset),
            size = Size(width = coreDiameter, height = coreDiameter),
            style = Stroke(strokeWidth / 2),
        )
    }
}

// Fractions of the ball's diameter rather than fixed dp, so every part scales together.
private const val OUTLINE_FRACTION = 0.04f
private const val OUTER_BUTTON_FRACTION = 0.25f
private const val INNER_BUTTON_FRACTION = 0.17f
private const val CORE_FRACTION = 0.10f
