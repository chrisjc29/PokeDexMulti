package com.unomaster.pokedexgame.network

import arrow.core.Either
import com.unomaster.pokedexgame.domain.error.DomainError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.ContentConvertException
// kotlinx.io, NOT io.ktor.utils.io.errors.IOException - Ktor 3 moved to kotlinx-io and deprecated
// the old alias. It still resolves today, but only until Ktor 4.
import kotlinx.io.IOException

// Wrap every remote call in this. This file is the only place in the app where an exception becomes
// a value: Ktor throws, the domain doesn't. Above this line nothing knows an HTTP status code
// exists, which is what lets toUserMessage word a rate limit differently from a crash without the
// presentation layer parsing exceptions.
//
// Either.catch is doing the work a hand-written try/catch used to. It is safe here in a way
// runCatching would not be: Arrow routes the catch through NonFatal, which explicitly treats
// CancellationException as fatal and rethrows it. Without that property a cancelled request would
// be converted into a "network failure" and the screen would show an error the user never caused.
suspend fun <T> networkCall(block: suspend () -> T): Either<DomainError, T> =
    Either.catch { block() }.mapLeft { it.toDomainError() }

// Private: the mapping is an implementation detail of this boundary. Nothing above the data layer
// should be given the vocabulary to ask about a Throwable again.
private fun Throwable.toDomainError(): DomainError = when (this) {
    is ClientRequestException ->
        if (response.status == HttpStatusCode.TooManyRequests) {
            DomainError.RateLimited(response.headers["Retry-After"]?.toLongOrNull())
        } else {
            DomainError.Unexpected(this)
        }

    // A 5xx reaches here as ServerResponseException because expectSuccess is on. It's the server's
    // bug, not a connectivity problem, so it is Unexpected via the else branch below - deliberately
    // not NetworkUnavailable, which would tell the user to check their connection.
    is HttpRequestTimeoutException -> DomainError.NetworkUnavailable(this)

    // MUST come before the IOException branch. Ktor 3's ContentConvertException — what a JSON
    // payload the DTOs can't parse arrives as — is itself an IOException, so ordering these the
    // other way round reports a broken response as "check your connection", sending the user to
    // fix their wifi over a bug in this app. A parse failure is a defect: Unexpected keeps the
    // Throwable so the stack trace reaches crash reporting.
    is ContentConvertException -> DomainError.Unexpected(this)

    // DNS failure, no route, connection reset — everything the transport couldn't complete.
    is IOException -> DomainError.NetworkUnavailable(this)

    // Deserialization failures, programming errors: real bugs. Unexpected keeps the Throwable so
    // the stack trace still reaches crash reporting.
    else -> DomainError.Unexpected(this)
}
