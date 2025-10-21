package com.unomaster.pokedexgame.di

import com.unomaster.pokedexgame.network.PokemonService
import com.unomaster.pokedexgame.network.PokemonServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkDependencies = module {
    single { provideBaseUrl() }
    single { provideCustomJson() }
    single { provideClient() }
    single<PokemonService> { PokemonServiceImpl(get()) }
}


private fun provideBaseUrl(): String = baseUrl

const val baseUrl = "https://pokeapi.co/api/v2/pokemon/"
private fun provideCustomJson(): Json {
    return Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}

fun provideClient() = HttpClient {
    install(DefaultRequest)
    install(Logging) {
        logger = Logger.DEFAULT
    }
    install(ContentNegotiation) {
        json(provideCustomJson())
    }
}