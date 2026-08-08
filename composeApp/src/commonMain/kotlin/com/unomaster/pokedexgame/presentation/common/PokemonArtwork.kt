package com.unomaster.pokedexgame.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors
import com.unomaster.pokedexgame.presentation.theme.Motion

// The silhouette is produced by tinting the artwork flat rather than by loading a second image, so
// the reveal is a colour-filter change on an image that is already in memory. The reveal cross-fades
// rather than cutting: the tinted copy is drawn over the full-colour one and faded out, which is a
// change of alpha on one layer instead of a re-decode. Both AsyncImages share a model, so Coil
// fetches once.
//
// isRevealed is a plain boolean and the colour is resolved here, at render time — the ViewModel
// never picks a colour, which is what keeps GameState theme-agnostic.
@Composable
fun PokemonArtwork(
    artworkUrl: String?,
    isRevealed: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.ArtworkSize,
) {
    val colors = LocalAppColors.current
    val silhouetteAlpha by animateFloatAsState(
        targetValue = if (isRevealed) 0f else 1f,
        animationSpec = tween(Motion.DurationReveal),
        label = "reveal",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkUrl == null) {
            // No URL means nothing has loaded yet — and it is also what previews and screenshot
            // goldens pass, so the regression tier never touches the network.
            Pokeball(size = size / 2)
        } else {
            AsyncImage(
                model = artworkUrl,
                contentDescription = if (isRevealed) "Revealed Pokemon" else "Pokemon silhouette",
                modifier = Modifier.fillMaxSize(),
            )
            if (silhouetteAlpha > 0f) {
                AsyncImage(
                    model = artworkUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(silhouetteAlpha),
                    colorFilter = ColorFilter.tint(colors.silhouette),
                )
            }
        }
    }
}
