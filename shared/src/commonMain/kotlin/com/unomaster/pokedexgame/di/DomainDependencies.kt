package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.domain.PokemonRepository
import com.unomaster.pokedexgame.domain.PokemonRepositoryImpl
import org.koin.dsl.module

val domainDependencies = module {
    includes(networkDependencies)
    single<PokemonRepository> { PokemonRepositoryImpl(get(), get()) }
}