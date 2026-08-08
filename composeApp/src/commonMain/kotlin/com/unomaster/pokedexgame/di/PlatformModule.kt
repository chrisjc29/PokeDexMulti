package com.unomaster.pokedexgame.di

import org.koin.core.module.Module

// Each platform provides only what it alone can build: the Ktor engine, the clock, the key-value
// store, and the analytics implementation. Common code sees interfaces, never platform types.
expect val platformModule: Module
