package com.unomaster.pokedexgame.presentation.settings

import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakeKeyValueStore
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        keyValueStore: FakeKeyValueStore = FakeKeyValueStore(),
        navigator: FakeAppNavigator = FakeAppNavigator(),
    ) = SettingsViewModel(
        keyValueStore = keyValueStore,
        navigator = navigator,
        crashReporter = NoopCrashReporter,
    )

    @Test
    fun readsThePersistedPreferenceOnInit() = runTest {
        val settingsViewModel = viewModel(
            FakeKeyValueStore(mapOf("analytics_enabled" to false)),
        )

        testScheduler.advanceUntilIdle()

        assertFalse(settingsViewModel.state.value.isAnalyticsEnabled)
    }

    @Test
    fun defaultsToEnabled_whenNothingIsStored() = runTest {
        val settingsViewModel = viewModel()

        testScheduler.advanceUntilIdle()

        assertEquals(true, settingsViewModel.state.value.isAnalyticsEnabled)
    }

    @Test
    fun togglingPersistsTheNewValue() = runTest {
        val store = FakeKeyValueStore()
        val settingsViewModel = viewModel(store)
        testScheduler.advanceUntilIdle()

        settingsViewModel.onIntent(SettingsIntent.AnalyticsToggled(isEnabled = false))

        assertFalse(settingsViewModel.state.value.isAnalyticsEnabled)
        assertFalse(store.getBoolean("analytics_enabled", default = true))
    }

    @Test
    fun backClicked_popsTheStack() = runTest {
        val navigator = FakeAppNavigator()
        val settingsViewModel = viewModel(navigator = navigator)
        testScheduler.advanceUntilIdle()

        settingsViewModel.onIntent(SettingsIntent.BackClicked)

        assertEquals(1, navigator.goBackCount)
    }
}
