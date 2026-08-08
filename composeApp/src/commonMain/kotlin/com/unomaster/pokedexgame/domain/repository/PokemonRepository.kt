package com.unomaster.pokedexgame.domain.repository

import arrow.core.Either
import com.unomaster.pokedexgame.domain.error.DomainError
import com.unomaster.pokedexgame.domain.model.PokemonQuestion

// Either<DomainError, PokemonQuestion>, not PokemonQuestion-or-throw. The failure cases are in the
// type, so a caller cannot forget that this can fail, and the compiler names the ones it must
// consider.
//
// pageUrl is null for the first round; every later round passes the previous question's
// nextPageUrl. The base URL is a data-layer detail and never reaches presentation.
interface PokemonRepository {
    suspend fun getQuestion(pageUrl: String?): Either<DomainError, PokemonQuestion>
}
