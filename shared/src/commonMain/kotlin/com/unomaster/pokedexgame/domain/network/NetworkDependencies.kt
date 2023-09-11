package com.unomaster.pokedexgame.domain.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkDependencies {

    const val baseUrl = "https://pokeapi.co/api/v2/pokemon/"
    private fun provideCustomJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    val client = HttpClient() {
        install(DefaultRequest)
        install(Logging) {
            logger = Logger.DEFAULT
        }
        install(ContentNegotiation) {
            json(provideCustomJson())
        }
    }
}