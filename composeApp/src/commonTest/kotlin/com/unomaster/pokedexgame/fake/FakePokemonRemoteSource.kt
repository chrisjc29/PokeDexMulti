package com.unomaster.pokedexgame.fake

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.unomaster.pokedexgame.data.dto.PokemonDetailDto
import com.unomaster.pokedexgame.data.dto.PokemonPageDto
import com.unomaster.pokedexgame.data.remote.PokemonRemoteSource
import com.unomaster.pokedexgame.domain.error.DomainError

// Two failure knobs rather than one: the repository makes two calls, and the interesting bug is a
// page that succeeds followed by a detail call that doesn't.
class FakePokemonRemoteSource(
    private val page: PokemonPageDto = pokemonPageDto(),
    private val detail: PokemonDetailDto = pokemonDetailDto(),
    private val pageFailsWith: DomainError? = null,
    private val detailFailsWith: DomainError? = null,
) : PokemonRemoteSource {

    val requestedDetailUrls = mutableListOf<String>()

    override suspend fun fetchPage(pageUrl: String?): Either<DomainError, PokemonPageDto> =
        pageFailsWith?.left() ?: page.right()

    override suspend fun fetchDetail(detailUrl: String): Either<DomainError, PokemonDetailDto> {
        requestedDetailUrls += detailUrl
        return detailFailsWith?.left() ?: detail.right()
    }
}
