package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.data.random.KotlinRandomSource
import com.unomaster.pokedexgame.data.remote.KtorPokemonRemoteSource
import com.unomaster.pokedexgame.data.remote.PokemonRemoteSource
import com.unomaster.pokedexgame.data.repository.PokemonRepositoryImpl
import com.unomaster.pokedexgame.domain.random.RandomSource
import com.unomaster.pokedexgame.domain.repository.PokemonRepository
import com.unomaster.pokedexgame.domain.usecase.GetPokemonQuestionUseCase
import com.unomaster.pokedexgame.network.createHttpClient
import com.unomaster.pokedexgame.presentation.game.GameViewModel
import com.unomaster.pokedexgame.presentation.home.HomeViewModel
import com.unomaster.pokedexgame.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    // Networking — the Ktor client (engine comes from platformModule).
    single { createHttpClient(get()) }

    // Data layer
    singleOf(::KtorPokemonRemoteSource) bind PokemonRemoteSource::class
    singleOf(::PokemonRepositoryImpl) bind PokemonRepository::class
    // Randomness is injected for the same reason the clock is — see RandomSource.
    single { KotlinRandomSource() } bind RandomSource::class

    // Domain layer
    factoryOf(::GetPokemonQuestionUseCase)

    // Presentation layer (MVI ViewModels) — one per screen, never a god ViewModel.
    viewModelOf(::HomeViewModel)
    viewModelOf(::GameViewModel)
    viewModelOf(::SettingsViewModel)
}
