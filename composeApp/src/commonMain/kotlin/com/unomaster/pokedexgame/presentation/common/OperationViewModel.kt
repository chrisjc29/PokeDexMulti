package com.unomaster.pokedexgame.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.flatten
import com.unomaster.pokedexgame.analytics.CrashReporter
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.error.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// operation() is the only way work leaves a ViewModel, so all four responsibilities are settled in
// one place:
//   - cancellation propagates instead of being handled as a failure
//   - every failure worth reporting reaches the crash reporter exactly once
//   - the message shown is decided by toUserMessage, not by each screen
//   - the success path is straight-line code with no error plumbing in it
abstract class OperationViewModel(
    private val crashReporter: CrashReporter,
) : ViewModel() {

    // The block returns Either<DomainError, Unit> — in practice an `either { }` block whose body
    // binds the use cases it needs. A body that genuinely cannot fail still ends with either { },
    // which keeps one shape rather than two.
    //
    // fallbackMessage is what the user sees when the failure isn't one the app recognises. It
    // describes what was being attempted ("Could not load a Pokemon"), which is the part only the
    // calling screen knows.
    protected fun operation(
        fallbackMessage: String,
        onFailure: (String) -> Unit,
        block: suspend () -> Either<DomainError, Unit>,
    ): Job = viewModelScope.launch {
        // Either.catch is the outer net for a body that throws where it was supposed to return a
        // Left — a bug, or a layer not yet converted. It is NOT the cancellation risk runCatching
        // would be here: Arrow's NonFatal classifies CancellationException as fatal and rethrows,
        // so a cancelled screen still cancels instead of rendering an error the user never caused.
        val outcome: Either<DomainError, Unit> =
            Either.catch { block() }
                .mapLeft { DomainError.Unexpected(it) }
                .flatten()

        outcome.onLeft { error ->
            crashReporter.record(error)
            onFailure(error.toUserMessage(fallbackMessage))
        }
    }

    // For reads the screen can do without. Still reported, so a storage problem is visible in crash
    // reporting rather than silently absorbed.
    // Fully qualified: an unqualified call would resolve to this member and recurse.
    protected suspend fun <T> bestEffort(fallback: T, block: suspend () -> T): T =
        com.unomaster.pokedexgame.domain.error.bestEffort(
            fallback = fallback,
            onFailure = { crashReporter.record(it) },
            block = block,
        )
}

// Which failures are worth a crash report, decided once. A rate limit and an empty response are the
// server telling us something, not defects — recording them buries the real crashes in noise. This
// is a distinction a Throwable-based version could not make, because everything arrived as an
// exception and every exception looked like a bug.
private fun CrashReporter.record(error: DomainError) {
    when (error) {
        is DomainError.Unexpected -> recordException(error.cause)
        is DomainError.NetworkUnavailable -> error.cause?.let { recordException(it) }
        is DomainError.RateLimited, DomainError.EmptyResponse -> Unit
    }
}
