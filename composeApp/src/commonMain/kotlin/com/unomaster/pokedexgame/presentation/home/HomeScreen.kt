package com.unomaster.pokedexgame.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.DevicePrimaryKey
import com.unomaster.pokedexgame.presentation.common.DeviceScaffold
import com.unomaster.pokedexgame.presentation.common.DeviceScreenPanel
import com.unomaster.pokedexgame.presentation.common.Pokeball
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.DexTypography
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Fires when the screen enters composition — on launch, and again each time a run ends and the
    // player comes back — so the best streak they just set is the one they see.
    LaunchedEffect(Unit) { viewModel.onIntent(HomeIntent.Shown) }

    HomeContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current

    DeviceScaffold(
        modifier = modifier,
        // The stamped label is the way into Settings. The dex has no toolbar to hang a button on,
        // and this is the only chrome text on the screen, so it is where a thumb goes looking.
        metaLabel = "SETTINGS",
        metaContentDescription = "Settings",
        onMetaClick = { onIntent(HomeIntent.SettingsClicked) },
        caseGap = Dimens.DeviceCaseGapLarge,
    ) {
        DeviceScreenPanel(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // heightIn(min = maxHeight) inside the scroll is what lets this be centred *and* safe:
            // when the LCD is tall enough the column is exactly the viewport and the group sits in
            // the middle, and when it isn't — landscape, or a large font scale — the column grows
            // and scrolls instead of clipping the title and the streak line out of existence.
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(Dimens.DeviceScreenPadding),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        Dimens.DeviceScreenGap,
                        Alignment.CenterVertically,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = maxHeight),
                ) {
                    Pokeball(size = Dimens.HomePokeballSize)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpacingExtraSmall),
                    ) {
                        Text(
                            text = "POKÉDEX GAME",
                            style = DexTypography.Title,
                            color = colors.deviceScreenInk,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Guess the Pokémon from its silhouette.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.deviceScreenInkDim,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.widthIn(max = Dimens.HomeTaglineMaxWidth),
                        )
                    }

                    // Left off entirely until the player has actually set one — an honest blank
                    // beats `BEST STREAK · 0`, which reads as a scoreboard telling you that you are
                    // bad at this before you have played.
                    if (state.bestStreak > 0) {
                        Text(
                            text = "BEST STREAK · ${state.bestStreak}",
                            style = DexTypography.SectionLabel,
                            color = colors.deviceScreenInkFaint,
                        )
                    }
                }
            }
        }

        DevicePrimaryKey(
            label = "START GAME",
            onClick = { onIntent(HomeIntent.StartGameClicked) },
        )
    }
}

// Previews double as the source for Roborazzi's regression screenshots. Each preview = one generated
// screenshot test, so every state the screen can be in gets one.
@Preview
@Composable
fun HomeContentPreview() {
    AppTheme { HomeContent(state = HomeState(bestStreak = 12), onIntent = {}) }
}

@Preview
@Composable
fun HomeContentNoStreakPreview() {
    AppTheme { HomeContent(state = HomeState(), onIntent = {}) }
}
