package com.unomaster.pokedexgame.presentation.home

import com.unomaster.pokedexgame.data.local.BestStreakStore
import com.unomaster.pokedexgame.fake.FakeAppNavigator
import com.unomaster.pokedexgame.fake.FakeKeyValueStore
import com.unomaster.pokedexgame.fake.NoopCrashReporter
import com.unomaster.pokedexgame.navigation.AppRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(
        navigator: FakeAppNavigator = FakeAppNavigator(),
        keyValueStore: FakeKeyValueStore = FakeKeyValueStore(),
    ) = HomeViewModel(
        navigator = navigator,
        bestStreakStore = BestStreakStore(keyValueStore),
        crashReporter = NoopCrashReporter,
    )

    @Test
    fun startGame_navigatesToTheGame() {
        val navigator = FakeAppNavigator()

        viewModel(navigator).onIntent(HomeIntent.StartGameClicked)

        assertEquals(listOf<AppRoute>(AppRoute.Game), navigator.visitedRoutes)
    }

    @Test
    fun settings_navigatesToSettings() {
        val navigator = FakeAppNavigator()

        viewModel(navigator).onIntent(HomeIntent.SettingsClicked)

        assertEquals(listOf<AppRoute>(AppRoute.Settings), navigator.visitedRoutes)
    }

    @Test
    fun shown_readsThePersistedBestStreak() = runTest {
        val homeViewModel = viewModel(keyValueStore = FakeKeyValueStore(mapOf("best_streak" to "12")))

        homeViewModel.onIntent(HomeIntent.Shown)
        testScheduler.advanceUntilIdle()

        assertEquals(12, homeViewModel.state.value.bestStreak)
    }

    // Re-read every time the screen appears, not once on construction: the ViewModel outlives a trip
    // to the game, so a streak set during that trip has to land here on the way back.
    @Test
    fun shown_picksUpAStreakSetSinceTheLastVisit() = runTest {
        val store = FakeKeyValueStore()
        val homeViewModel = viewModel(keyValueStore = store)
        homeViewModel.onIntent(HomeIntent.Shown)
        testScheduler.advanceUntilIdle()

        store.putString("best_streak", "3")
        homeViewModel.onIntent(HomeIntent.Shown)
        testScheduler.advanceUntilIdle()

        assertEquals(3, homeViewModel.state.value.bestStreak)
    }

    @Test
    fun noStoredStreak_readsAsZero() = runTest {
        val homeViewModel = viewModel()

        homeViewModel.onIntent(HomeIntent.Shown)
        testScheduler.advanceUntilIdle()

        assertEquals(0, homeViewModel.state.value.bestStreak)
    }
}
