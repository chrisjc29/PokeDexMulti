package com.unomaster.pokedexgame.ui

import PokeDexGameTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.unomaster.pokedexgame.domain.State
import com.unomaster.pokedexgame.domain.models.CombinedPokemonResponse
import com.unomaster.pokedexgame.domain.network.NetworkDependencies
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PokeDexGame() {
    val pokemonViewModel: PokemonViewModel = kmpViewModel(
        factory = viewModelFactory {
            PokemonViewModel(savedStateHandle = createSavedStateHandle())
        },
    )
    val combinedPokemonResponse by pokemonViewModel.pokemonRequest.collectAsState()
    val backgroundColor = if (isSystemInDarkTheme()) Color(102, 178, 255)
    else Color(102, 178, 255)
    val progressColor = if (isSystemInDarkTheme()) Color.Black else Color.Black
    val textColor = if (isSystemInDarkTheme()) Color.Black else Color.Black
    val windowSizeClass = calculateWindowSizeClass()


    LaunchedEffect(Unit) {
        pokemonViewModel.startGame(NetworkDependencies.baseUrl)
    }

    val screenModifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .padding(12.dp)
        .background(backgroundColor, RoundedCornerShape(8.dp))
        .border(
            8.dp,
            Color(0, 0, 204),
            RoundedCornerShape(8.dp)
        )

    PokeDexGameTheme {
        Column(
            screenModifier
        ) {
            when (val result = combinedPokemonResponse) {
                is State.Success<CombinedPokemonResponse> -> {
                    val isWinner = remember { pokemonViewModel._winner }

                    when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
                            GameModeLandscape(
                                pokemonViewModel,
                                isWinner,
                                textColor,
                                result
                            )
                        }

                        WindowWidthSizeClass.Compact -> {
                            GameModePortrait(
                                pokemonViewModel,
                                isWinner,
                                textColor,
                                result
                            )
                        }
                    }
                }

                is State.Error -> {
                    Text(result.error)
                }

                is State.Loading -> {
                    Column(
                        Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(120.dp),
                            color = progressColor
                        )
                    }

                }
            }
        }
    }
}

@Composable
private fun GameModePortrait(
    pokemonViewModel: PokemonViewModel,
    isWinner: MutableStateFlow<Boolean>,
    textColor: Color,
    state: State.Success<CombinedPokemonResponse>
) {
    Column(
        Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(!isWinner.collectAsState().value) {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Who's that pokemon?",
                    fontSize = 36.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        val pokemonBitmap by remember { state.data.pokemonBitmap }
        PokemonImage(
            pokemonBitmap,
            pokemonViewModel
        )

        RestartGame(
            pokemonViewModel,
            state.data.pokemonApiResponse.next,
        )

        val multipleChoiceList by remember { state.data.multipleChoiceList }

        MultipleChoiceContainer(
            multipleChoiceList,
            isWinner,
        ) { selectPokemonName ->
            val pokemonNameFromApi =
                state.data.pokemonDetailsResponse.name.capitalize(Locale.current)
            pokemonViewModel.handleMultipleItemChoiceState(
                selectPokemonName, pokemonNameFromApi
            )
        }
    }
}

@Composable
private fun GameModeLandscape(
    pokemonViewModel: PokemonViewModel,
    isWinner: MutableStateFlow<Boolean>,
    textColor: Color,
    state: State.Success<CombinedPokemonResponse>
) {
    Row(
        Modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(!isWinner.collectAsState().value) {
            Column(
                Modifier.weight(0.5f).padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Who's that pokemon?",
                    fontSize = 36.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                val pokemonBitmap by remember { state.data.pokemonBitmap }
                PokemonImage(
                    pokemonBitmap,
                    pokemonViewModel
                )
            }
        }


        RestartGame(
            pokemonViewModel,
            state.data.pokemonApiResponse.next,
        )

        val multipleChoiceList by remember { state.data.multipleChoiceList }

        MultipleChoiceContainer(
            multipleChoiceList,
            isWinner,
        ) { selectPokemonName ->
            val pokemonNameFromApi =
                state.data.pokemonDetailsResponse.name.capitalize(Locale.current)
            pokemonViewModel.handleMultipleItemChoiceState(
                selectPokemonName, pokemonNameFromApi
            )
        }
    }
}
