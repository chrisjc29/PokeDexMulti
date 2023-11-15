package com.unomaster.pokedexgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel

@Composable
fun PokemonImage(
    pokemonBitmap: ImageBitmap?,
    pokemonViewModel: PokemonViewModel
) {
    val colorFilter = remember { pokemonViewModel.overlay }

    pokemonBitmap?.let {
        Image(
            it,
            null,
            modifier = Modifier.size(300.dp).background(
                Brush.radialGradient(
                listOf(
                    Color.White,
                    Color.Transparent
                ),
                center = Offset.Unspecified,
                radius = LocalDensity.current.density * 150,
                tileMode = TileMode.Clamp
            )),
            colorFilter = colorFilter.collectAsState().value
        )
    }
}
