package com.unomaster.pokedexgame.domain

sealed class MultipleChoiceRotationState(private val angle: Float) {
    data class Front(val angle: Float = 0f): MultipleChoiceRotationState(angle) {
        val next: MultipleChoiceRotationState
            get() = Back()
    }

    data class Back(val angle: Float = 180f): MultipleChoiceRotationState(angle) {
        val next: MultipleChoiceRotationState
            get() = Front()
    }
}