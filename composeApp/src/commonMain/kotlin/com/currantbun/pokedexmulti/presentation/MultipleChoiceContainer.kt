package com.unomaster.pokedexgame.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MultipleChoiceContainer(
    multipleChoiceList: List<String>,
    isWinner: MutableStateFlow<Boolean>,
    onClick: (name: String) -> Unit
) {
    AnimatedVisibility(!isWinner.collectAsState().value) {
        Column(Modifier.padding(vertical = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MultipleChoiceItem(multipleChoiceList[0], onClick )
                MultipleChoiceItem(multipleChoiceList[1], onClick)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MultipleChoiceItem(multipleChoiceList[2], onClick)
                MultipleChoiceItem(multipleChoiceList[3], onClick)
            }
        }
    }
}