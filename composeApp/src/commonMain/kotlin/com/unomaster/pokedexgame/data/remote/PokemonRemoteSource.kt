package com.unomaster.pokedexgame.data.remote

import arrow.core.Either
import com.unomaster.pokedexgame.data.dto.PokemonDetailDto
import com.unomaster.pokedexgame.data.dto.PokemonPageDto
import com.unomaster.pokedexgame.domain.error.DomainError

// The repository talks to this interface, not to Ktor directly, so it can be faked in tests.
// The Ktor-backed implementation is KtorPokemonRemoteSource.
//
// The Either starts here: this is the innermost point that knows a call can fail, so it is where
// the failure becomes a value. Everything above just binds it.
interface PokemonRemoteSource {
    // pageUrl is null for the first page; the implementation owns the base URL.
    suspend fun fetchPage(pageUrl: String?): Either<DomainError, PokemonPageDto>

    suspend fun fetchDetail(detailUrl: String): Either<DomainError, PokemonDetailDto>
}
