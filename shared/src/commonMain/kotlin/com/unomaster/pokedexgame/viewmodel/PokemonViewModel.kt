package com.unomaster.pokedexgame.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.hoc081098.kmp.viewmodel.SavedStateHandle
import com.hoc081098.kmp.viewmodel.ViewModel
import com.unomaster.pokedexgame.domain.PokemonRepository
import com.unomaster.pokedexgame.domain.State
import com.unomaster.pokedexgame.domain.models.CombinedPokemonResponse
import com.unomaster.pokedexgame.domain.network.NetworkDependencies
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class PokemonViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val pokemonRepository: PokemonRepository = PokemonRepository(),
): ViewModel() {
    private val _pokemonRequest = savedStateHandle.getStateFlow("url", NetworkDependencies.baseUrl)

    val _winner = MutableStateFlow<Boolean>(false)
    val _overlay = MutableStateFlow<ColorFilter?>(ColorFilter.tint(Color.LightGray))

    @OptIn(ExperimentalCoroutinesApi::class)
    val pokemonRequest: StateFlow<State<CombinedPokemonResponse, String>> =
        _pokemonRequest.flatMapLatest {
            pokemonRepository.fetchPokemon(it)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            State.Loading
        )

    fun startGame(url: String) {
        savedStateHandle["url"] = url
    }

    fun restartGame(url: String) {
        savedStateHandle["url"] = url
        _winner.update { false }
        _overlay.update { ColorFilter.tint(Color.LightGray) }
    }

    fun handleMultipleItemChoiceState(selectPokemonName: String, pokemonNameFromApi: String) {
        if (selectPokemonName == pokemonNameFromApi) {
            _winner.update { true }
            _overlay.update { null }
        } else {
            _winner.update { false }
            _overlay.update { ColorFilter.tint(Color.LightGray) }
        }
    }

}