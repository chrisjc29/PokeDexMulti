package com.currantbun.pokedexmulti.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.currantbun.pokedexmulti.data.dependencies
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    MaterialTheme {
        KoinApplication({  modules(listOf(dependencies)) }) {
            PokeDexGame()
        }
    }
}