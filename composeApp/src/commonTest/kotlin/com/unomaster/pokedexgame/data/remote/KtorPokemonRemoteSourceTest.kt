package com.unomaster.pokedexgame.data.remote

import arrow.core.getOrElse
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.network.createHttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// The only test that exercises the real transport boundary: createHttpClient's configuration plus
// networkCall's Throwable -> DomainError mapping. Everything above the data layer is tested against
// the PokemonRemoteSource interface instead, which is why this file is small and this file alone.
class KtorPokemonRemoteSourceTest {

    // Every case runs its body on a real dispatcher. runTest's virtual clock skips idle time
    // instantly, and createHttpClient installs HttpTimeout — so on the test scheduler the 30s
    // request timeout "expires" the moment the call suspends, and every assertion here fails with
    // HttpRequestTimeoutException instead of exercising the mapping it was written for.
    private fun networkTest(block: suspend () -> Unit) = runTest {
        withContext(Dispatchers.Default) { block() }
    }

    private fun sourceReturning(
        body: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        headers: io.ktor.http.Headers = headersOf(HttpHeaders.ContentType, "application/json"),
    ) = KtorPokemonRemoteSource(
        createHttpClient(MockEngine { respond(content = body, status = status, headers = headers) }),
    )

    @Test
    fun parsesAPage() = networkTest {
        val source = sourceReturning(
            """
            {
              "next": "https://pokeapi.co/api/v2/pokemon/?offset=20",
              "results": [{ "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/" }]
            }
            """.trimIndent(),
        )

        val page = source.fetchPage(pageUrl = null)
            .getOrElse { throw AssertionError("expected a page, got $it") }

        assertEquals("https://pokeapi.co/api/v2/pokemon/?offset=20", page.next)
        assertEquals("bulbasaur", page.results.single().name)
    }

    @Test
    fun parsesTheOfficialArtworkOutOfTheDetailPayload() = networkTest {
        // The real payload is enormous; this proves the small DTO survives ignoreUnknownKeys and
        // that the hyphenated "official-artwork" key is mapped.
        val source = sourceReturning(
            """
            {
              "id": 25,
              "name": "pikachu",
              "weight": 60,
              "sprites": {
                "front_default": "https://example.test/front.png",
                "other": {
                  "official-artwork": { "front_default": "https://example.test/art.png" }
                }
              }
            }
            """.trimIndent(),
        )

        val detail = source.fetchDetail("https://example.test/pokemon/25")
            .getOrElse { throw AssertionError("expected a detail, got $it") }

        assertEquals(25, detail.id)
        assertEquals("https://example.test/art.png", detail.sprites.other.officialArtwork.frontDefault)
    }

    // expectSuccess = true is what turns a 429 into a ClientRequestException at all; without it the
    // body would just fail to deserialize and this would be an Unexpected instead.
    @Test
    fun rateLimitCarriesTheRetryAfterHeader() = networkTest {
        val source = sourceReturning(
            body = "",
            status = HttpStatusCode.TooManyRequests,
            headers = headersOf(HttpHeaders.RetryAfter, "42"),
        )

        assertEquals(DomainError.RateLimited(retryAfterSeconds = 42), source.fetchPage(null).leftOrNull())
    }

    @Test
    fun transportFailureBecomesNetworkUnavailable() = networkTest {
        val source = KtorPokemonRemoteSource(
            createHttpClient(MockEngine { throw IOException("no route to host") }),
        )

        assertIs<DomainError.NetworkUnavailable>(source.fetchPage(null).leftOrNull())
    }

    // A response the app cannot parse is a bug, not a connectivity problem — telling the user to
    // check their connection would be wrong.
    @Test
    fun malformedPayloadBecomesUnexpected() = networkTest {
        val source = sourceReturning("{ not json at all }")

        val error = source.fetchDetail("https://example.test/pokemon/1").leftOrNull()

        assertIs<DomainError.Unexpected>(error)
    }
}
