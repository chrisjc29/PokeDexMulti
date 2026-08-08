package com.unomaster.pokedexgame

import android.app.Application

// Robolectric instantiates the manifest's android:name Application for every test. That is
// MainApplication, whose onCreate() calls startKoin — and Koin's container is static, so the second
// test in a sandbox fails with KoinApplicationAlreadyStartedException. The failure names Koin rather
// than the real cause, which is that a UI test has no business booting the app's DI graph.
//
// These tests construct their ViewModels directly and never resolve anything from Koin, so they run
// against an Application that does nothing at all.
class TestApplication : Application()
