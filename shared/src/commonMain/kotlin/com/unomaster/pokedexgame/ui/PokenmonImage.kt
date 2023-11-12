package com.unomaster.pokedexgame.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel

@Composable
fun PokemonImage(
    pokemonBitmap: ImageBitmap?,
    pokemonViewModel: PokemonViewModel
) {
    val colorFilter = remember { pokemonViewModel._overlay }

    pokemonBitmap?.let {
        Image(
            it,
            null,
            modifier = Modifier.size(300.dp),
            colorFilter = colorFilter.collectAsState().value
        )
    }
}
