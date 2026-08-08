package com.unomaster.pokedexgame.domain.error

import arrow.core.Either
import arrow.core.getOrElse

// For work whose failure is survivable: a preference read that only decides whether a switch starts
// on. The caller carries on with the fallback.
//
// This is the bridge for code that still throws — a platform key-value store, a third-party SDK.
// Anything that already returns Either doesn't need it: `either.getOrElse { fallback }` says the
// same thing, which is the point of moving failure into the type.
//
// Why Either.catch and not runCatching: runCatching catches Throwable, and CancellationException is
// a Throwable. Inside a coroutine it swallows the signal telling the coroutine to stop — the body is
// cancelled, the exception becomes a Result.failure, and the coroutine "handles" it and completes
// normally while its parent still considers it cancelled. Arrow's Either.catch routes through
// NonFatal, which explicitly classifies CancellationException as fatal and rethrows it, so the
// cancellation signal survives. That is a property of the library, not something this file adds.
//
// onFailure is how a caller that can report — a ViewModel with a crash reporter — stays informed;
// survivable is not the same as invisible.
suspend fun <T> bestEffort(
    fallback: T,
    onFailure: (DomainError) -> Unit = {},
    block: suspend () -> T,
): T = Either.catch { block() }
    .mapLeft { DomainError.Unexpected(it) }
    .onLeft(onFailure)
    .getOrElse { fallback }
