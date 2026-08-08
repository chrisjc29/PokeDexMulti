package com.unomaster.pokedexgame.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.ScreenScaffold
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.Dimens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SettingsContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (SettingsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ScreenScaffold(
        title = "Settings",
        modifier = modifier,
        onNavigateBack = { onIntent(SettingsIntent.BackClicked) },
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Dimens.ScreenPadding)
                .fillMaxWidth()
                .heightIn(min = Dimens.MinimumTouchTarget),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Share analytics", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = state.isAnalyticsEnabled,
                onCheckedChange = { onIntent(SettingsIntent.AnalyticsToggled(it)) },
            )
        }
    }
}

@Preview
@Composable
fun SettingsContentEnabledPreview() {
    AppTheme { SettingsContent(state = SettingsState(isAnalyticsEnabled = true), onIntent = {}) }
}

@Preview
@Composable
fun SettingsContentDisabledPreview() {
    AppTheme { SettingsContent(state = SettingsState(isAnalyticsEnabled = false), onIntent = {}) }
}
