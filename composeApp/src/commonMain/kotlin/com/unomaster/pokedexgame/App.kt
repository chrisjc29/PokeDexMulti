package com.unomaster.pokedexgame

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.unomaster.pokedexgame.navigation.AppNavDisplay
import com.unomaster.pokedexgame.presentation.theme.AppTheme
import io.ktor.client.HttpClient
import org.koin.compose.koinInject

// Deliberately not previewable: this resolves dependencies from Koin, so it can only run in a real
// app process. The screen-level *Content composables carry the previews instead.
@Composable
fun App() {
    val httpClient = koinInject<HttpClient>()

    // Coil is given the app's own Ktor client rather than building a second one, so timeouts and
    // the engine are configured in exactly one place. On Kotlin/Native there is no ServiceLoader to
    // discover the fetcher, so registering it explicitly is required, not optional.
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory(httpClient)) }
            .build()
    }

    AppTheme {
        AppNavDisplay()
    }
}
