package com.unomaster.pokedexgame.domain.error

// The one place that decides what a failure says to the user. Unexpected falls back to the wording
// the calling screen supplies, because only that screen knows what was being attempted.
//
// No else branch, on purpose. This when is exhaustive over the sealed interface, so adding a case to
// DomainError breaks this file — which is exactly where you want to be told.
fun DomainError.toUserMessage(fallback: String): String = when (this) {
    is DomainError.RateLimited -> "Too many requests. Give it a moment and try again."
    is DomainError.NetworkUnavailable -> "Couldn't connect. Check your connection and try again."
    DomainError.EmptyResponse -> "There's nothing here yet."
    is DomainError.Unexpected -> fallback
}
