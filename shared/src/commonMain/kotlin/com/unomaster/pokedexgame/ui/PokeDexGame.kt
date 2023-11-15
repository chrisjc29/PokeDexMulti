package com.unomaster.pokedexgame.ui

import PokeDexGameTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoc081098.kmp.viewmodel.compose.kmpViewModel
import com.hoc081098.kmp.viewmodel.createSavedStateHandle
import com.hoc081098.kmp.viewmodel.viewModelFactory
import com.unomaster.pokedexgame.di.baseUrl
import com.unomaster.pokedexgame.di.domainDependencies
import com.unomaster.pokedexgame.domain.PokemonRepository
import com.unomaster.pokedexgame.domain.State
import com.unomaster.pokedexgame.domain.models.CombinedPokemonResponse
import com.unomaster.pokedexgame.viewmodel.PokemonViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.KoinApplication
import org.koin.compose.rememberKoinInject


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun PokeDexGame() {
    KoinApplication(
        moduleList = {
            listOf(domainDependencies)
        }
    ) {
        val gameStart = remember { mutableStateOf(false) }
        val pokemonRepository = rememberKoinInject<PokemonRepository>()

        val pokemonViewModel: PokemonViewModel = kmpViewModel(
            factory = viewModelFactory {
                PokemonViewModel(
                    savedStateHandle = createSavedStateHandle(),
                    pokemonRepositoryImpl = pokemonRepository
                )
            },
        )
        val combinedPokemonResponse by pokemonViewModel.pokemonRequest.collectAsState()
        val backgroundColor = if (isSystemInDarkTheme()) Color(102, 178, 255)
        else Color(102, 178, 255)
        val progressColor = if (isSystemInDarkTheme()) Color.Black else Color.Black
        val textColor = if (isSystemInDarkTheme()) Color.Black else Color.Black
        val windowSizeClass = calculateWindowSizeClass()


        LaunchedEffect(Unit) {
            pokemonViewModel.startGame(baseUrl)
        }

        val screenModifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(
                12.dp,
                Color(0, 0, 204),
                RoundedCornerShape(8.dp)
            )

        PokeDexGameTheme {
            if(!gameStart.value) {
                Column(
                    screenModifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Pokeball()
                    Button(onClick = {
                        gameStart.value = true
                    }) {
                        Text("Start Game!")
                    }
                }
            }

            if(gameStart.value) {
                Column(
                    screenModifier
                ) {
                    when (val result = combinedPokemonResponse) {
                        is State.Success<CombinedPokemonResponse> -> {
                            val isWinner = remember { pokemonViewModel.winner }

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
                GameTitleText(textColor)
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


@Composable
fun Pokeball() {
    val sizedp = 300.dp
    val sizepx = with(LocalDensity.current) { sizedp.toPx() }
    Box(
        modifier = Modifier
            .padding(32.dp)
            .wrapContentSize()
    ) {
        val blackLineColor = Color.Black
        val strokeWidth = sizepx * .04f
        val outerBallPercentage = .25.toFloat()
        val innerBallPercentage = .17.toFloat()
        val innerestBallPercentage = .10.toFloat()
        Box(
            modifier = Modifier
                .width(sizedp)
                .height(sizedp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawArc(
                    brush = Brush.linearGradient(listOf(Color.White, Color.White)),
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false
                )
                drawArc(
                    brush = Brush.linearGradient(listOf(Color.Red, Color.Red)),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true
                )
                drawArc(
                    color = blackLineColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth)
                )

                drawLine(
                    color = blackLineColor,
                    start = Offset(
                        x = 0f,
                        y = (size.height / 2)
                    ),
                    end = Offset(
                        x = size.width,
                        y = (size.height / 2)
                    ),
                    strokeWidth = strokeWidth

                )
                val outerBallSizePx = sizepx * outerBallPercentage
                drawCircle(
                    color = Color.Black,
                    radius = outerBallSizePx / 2,
                )

                val innerBallSizePx = sizepx * innerBallPercentage
                drawCircle(
                    color = Color(193, 212, 227),
                    radius = innerBallSizePx / 2,
                )

                val innerestBallSizePx = sizepx * innerestBallPercentage
                val innerestBallMarginPx = ((sizepx - innerestBallSizePx) / 2)
                drawArc(
                    color = Color.Black,
                    startAngle = 0f,
                    sweepAngle = 360f, // * animateFloat.value,
                    useCenter = false,
                    topLeft = Offset(
                        x = innerestBallMarginPx,
                        y = innerestBallMarginPx,
                    ),
                    size = Size(
                        width = innerestBallSizePx,
                        height = innerestBallSizePx,
                    ),
                    style = Stroke(4f)
                )

            }
        }
    }
}