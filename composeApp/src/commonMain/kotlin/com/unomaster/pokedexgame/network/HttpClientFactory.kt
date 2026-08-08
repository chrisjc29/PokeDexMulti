package com.unomaster.pokedexgame.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// Engine is injected so common code never references OkHttp or Darwin. Everything else is shared.
fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        // REQUIRED for the error mapping in NetworkCall.kt to work at all. Ktor defaults this to
        // false, which means a 4xx is NOT an exception - body<T>() just tries to deserialize the
        // error payload and fails with a confusing serialization error instead. With it on, Ktor
        // throws ClientRequestException for 4xx and ServerResponseException for 5xx, which is what
        // toDomainError() pattern-matches on.
        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    // The Pokemon detail payload is enormous and mostly unused, so tolerating
                    // unknown keys isn't just defensive here — it's the only way the small DTO
                    // deserializes at all.
                    ignoreUnknownKeys = true
                    isLenient = true
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
    }
