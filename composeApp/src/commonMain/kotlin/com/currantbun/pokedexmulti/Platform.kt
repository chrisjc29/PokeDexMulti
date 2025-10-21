package com.currantbun.pokedexmulti

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform