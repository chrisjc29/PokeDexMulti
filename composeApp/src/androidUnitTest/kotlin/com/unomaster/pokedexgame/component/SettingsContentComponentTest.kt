package com.unomaster.pokedexgame.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.unomaster.pokedexgame.TestApplication
import com.unomaster.pokedexgame.presentation.settings.SettingsContent
import com.unomaster.pokedexgame.presentation.settings.SettingsIntent
import com.unomaster.pokedexgame.presentation.settings.SettingsState
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = TestApplication::class)
class SettingsContentComponentTest {

    @Test
    fun switchReflectsState() = runComposeUiTest {
        setContent {
            AppTheme {
                SettingsContent(state = SettingsState(isAnalyticsEnabled = true), onIntent = {})
            }
        }
        onNodeWithText("Share analytics").assertIsDisplayed()
        onNode(isToggleable()).assertIsOn()
    }

    @Test
    fun togglingSwitch_emitsIntentWithNewValue() = runComposeUiTest {
        var lastIntent: SettingsIntent? = null
        setContent {
            AppTheme {
                SettingsContent(
                    state = SettingsState(isAnalyticsEnabled = false),
                    onIntent = { lastIntent = it },
                )
            }
        }
        onNode(isToggleable()).assertIsOff()
        onNode(isToggleable()).performClick()
        waitForIdle()
        assertEquals(SettingsIntent.AnalyticsToggled(isEnabled = true), lastIntent)
    }
}
