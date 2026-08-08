package com.unomaster.pokedexgame.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.Pokeball
import com.unomaster.pokedexgame.presentation.common.ScreenScaffold
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.Dimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    HomeContent(onIntent = viewModel::onIntent)
}

@Composable
fun HomeContent(
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "PokeDex Game",
        modifier = modifier,
        actions = {
            TextButton(onClick = { onIntent(HomeIntent.SettingsClicked) }) { Text("Settings") }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Pokeball()
            Text(
                text = "Guess the Pokemon from its silhouette.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = { onIntent(HomeIntent.StartGameClicked) },
                modifier = Modifier.heightIn(min = Dimens.MinimumTouchTarget),
            ) {
                Text("Start game")
            }
        }
    }
}

@Preview
@Composable
fun HomeContentPreview() {
    AppTheme { HomeContent(onIntent = {}) }
}
