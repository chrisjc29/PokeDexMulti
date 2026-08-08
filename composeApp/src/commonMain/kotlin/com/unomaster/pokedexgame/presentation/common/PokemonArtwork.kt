package com.unomaster.pokedexgame.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import coil3.compose.AsyncImage
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors

// The silhouette is produced by tinting the artwork flat rather than by loading a second image, so
// the reveal is a colour-filter change on an image that is already in memory.
//
// isRevealed is a plain boolean and the colour is resolved here, at render time — the ViewModel
// never picks a colour, which is what keeps GameState theme-agnostic.
@Composable
fun PokemonArtwork(
    artworkUrl: String?,
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    Box(
        modifier = modifier.size(Dimens.ArtworkSize),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkUrl == null) {
            // No URL means nothing has loaded yet — and it is also what previews and screenshot
            // goldens pass, so the regression tier never touches the network.
            Pokeball(size = Dimens.ArtworkSize / 2)
        } else {
            AsyncImage(
                model = artworkUrl,
                contentDescription = if (isRevealed) "Revealed Pokemon" else "Pokemon silhouette",
                modifier = Modifier.fillMaxSize(),
                colorFilter = if (isRevealed) null else ColorFilter.tint(colors.silhouette),
            )
        }
    }
}
