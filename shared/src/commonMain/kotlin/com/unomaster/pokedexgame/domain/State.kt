package com.unomaster.pokedexgame.domain

sealed interface State<out Data, out Error> {
    data class Success<D>(val data: D):State<D, Nothing>
    data object Loading:State<Nothing, Nothing>
    data class Error<E>(val error: E):State<Nothing, E>
}
