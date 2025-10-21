package com.currantbun.pokedexmulti.data

import org.koin.dsl.module

val dataDependencies = module {
    includes(networkDependencies)
    single<PokemonRepository> { PokemonRepositoryImpl( get()) }
    single<PokemonService> { PokemonServiceImpl(get()) }
}