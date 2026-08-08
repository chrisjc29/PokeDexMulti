package com.unomaster.pokedexgame.di

// Called from Swift as KoinInitKt.doInitKoinIos() — Swift reserves init-prefixed names, so Kotlin's
// initKoinIos is exported with the `do` prefix.
fun initKoinIos() {
    initKoin()
}
