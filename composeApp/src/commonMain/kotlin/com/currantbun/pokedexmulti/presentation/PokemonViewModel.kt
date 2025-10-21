package com.currantbun.pokedexmulti.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.currantbun.pokedexmulti.data.baseUrl
import com.currantbun.pokedexmulti.data.PokemonRepository
import com.currantbun.pokedexmulti.data.State
import com.currantbun.pokedexmulti.data.networkModels.CombinedPokemonResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PokemonViewModel(
    private val pokemonRepositoryImpl: PokemonRepository,
): ViewModel() {
    private val _pokemonRequest = MutableStateFlow(baseUrl)

    val winner: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val overlay =
        MutableStateFlow<ColorFilter?>(ColorFilter.Companion.tint(Color.Companion.LightGray))

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonRequest: StateFlow<State<CombinedPokemonResponse, String>> =
        _pokemonRequest.flatMapLatest {
            pokemonRepositoryImpl.fetchPokemon(it)
        }.stateIn(
            viewModelScope,
            SharingStarted.Companion.WhileSubscribed(5000),
            State.Loading
        )

    fun startGame(url: String) {
        _pokemonRequest.update { url }
    }

    fun restartGame(url: String) {
        _pokemonRequest.update { url }
        winner.update { false }
        overlay.update { ColorFilter.Companion.tint(Color.Companion.LightGray) }
    }

    fun handleMultipleItemChoiceState(selectPokemonName: String, pokemonNameFromApi: String) {
        if (selectPokemonName == pokemonNameFromApi) {
            winner.update { true }
            overlay.update { null }
        } else {
            winner.update { false }
            overlay.update { ColorFilter.Companion.tint(Color.Companion.LightGray) }
        }
    }

}