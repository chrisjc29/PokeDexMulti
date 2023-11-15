package com.unomaster.pokedexgame.domain

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient


expect suspend fun loadImageFromUrl(url: String, client: HttpClient): ImageBitmap