package com.unomaster.pokedexgame.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.unomaster.pokedexgame.MainView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Firebase.initialize(this)
            MainView()
        }
    }
}

