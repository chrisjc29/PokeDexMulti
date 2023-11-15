package com.unomaster.pokedexgame.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun GameTitleText(textColor: Color) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        text = "Who's that pokemon?",
        fontSize = 30.sp,
        color = textColor,
        textAlign = TextAlign.Center
    )
}