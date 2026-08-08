package com.unomaster.pokedexgame.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unomaster.pokedexgame.presentation.common.DeviceScaffold
import com.unomaster.pokedexgame.presentation.common.DeviceScreenPanel
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import com.unomaster.pokedexgame.presentation.theme.Dimens
import com.unomaster.pokedexgame.presentation.theme.LocalAppColors
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
    val colors = LocalAppColors.current

    DeviceScaffold(
        modifier = modifier,
        title = "SETTINGS",
        // No indicator lights here: nothing on this screen is scanning, matching or failing, and
        // three lit lamps over a preferences panel would be saying something untrue.
        lights = null,
        onNavigateBack = { onIntent(SettingsIntent.BackClicked) },
    ) {
        DeviceScreenPanel(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.MinimumTouchTarget)
                    .padding(
                        horizontal = Dimens.SpacingMedium,
                        vertical = Dimens.DeviceCasePadding,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.DeviceLabelGap)) {
                    Text(
                        text = "Share analytics",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.deviceScreenInk,
                    )
                    Text(
                        text = "Helps fix crashes",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.deviceScreenInkFaint,
                    )
                }
                Switch(
                    checked = state.isAnalyticsEnabled,
                    onCheckedChange = { onIntent(SettingsIntent.AnalyticsToggled(it)) },
                    // Dressed from the device tokens rather than the ColorScheme: this switch sits
                    // on the LCD, so it has to read against pale green in either system theme.
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.deviceScan,
                        checkedTrackColor = colors.deviceScreenInk,
                        checkedBorderColor = colors.deviceScreenInk,
                        uncheckedThumbColor = colors.deviceScreen,
                        uncheckedTrackColor = colors.deviceScreenInk,
                        uncheckedBorderColor = colors.deviceScreenInk,
                    ),
                )
            }
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
