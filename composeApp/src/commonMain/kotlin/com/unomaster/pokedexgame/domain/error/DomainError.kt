package com.unomaster.pokedexgame.domain.error

// Every failure the app understands. Cases nest in the sealed parent → still one top-level type.
//
// Unexpected is the deliberate escape hatch for genuine bugs: it carries the Throwable so crash
// reporting keeps the stack trace, while callers still only ever see a DomainError.
sealed interface DomainError {
    data class RateLimited(val retryAfterSeconds: Long? = null) : DomainError
    data class NetworkUnavailable(val cause: Throwable? = null) : DomainError
    data object EmptyResponse : DomainError
    data class Unexpected(val cause: Throwable) : DomainError
}
