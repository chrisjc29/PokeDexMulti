package com.unomaster.pokedexgame.domain

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes


actual suspend fun loadImageFromUrl(url: String, client: HttpClient): ImageBitmap {
    val image = BitmapFactory.decodeStream(
        client.get(url).readBytes().inputStream()
    )
    return image.asImageBitmap()
}