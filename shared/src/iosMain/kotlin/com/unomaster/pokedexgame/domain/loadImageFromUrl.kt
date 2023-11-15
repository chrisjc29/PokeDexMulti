package com.unomaster.pokedexgame.domain

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import org.jetbrains.skia.Image

actual suspend fun loadImageFromUrl(url: String, client: HttpClient): ImageBitmap {
    val image = client.get(url).readBytes()
    return Image.makeFromEncoded(image).toComposeImageBitmap()
}