package com.currantbun.pokedexmulti.data

import com.currantbun.pokedexmulti.presentation.PokemonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dependencies = module {
    includes(networkDependencies)
    single<PokemonRepository> { PokemonRepositoryImpl( get()) }
    single<PokemonService> { PokemonServiceImpl(get()) }
    viewModel<PokemonViewModel> { PokemonViewModel(get()) }
}