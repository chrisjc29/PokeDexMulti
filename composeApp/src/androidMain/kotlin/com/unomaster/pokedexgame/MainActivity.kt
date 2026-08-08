package com.unomaster.pokedexgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

// ComponentActivity, not AppCompatActivity: the app draws entirely with Compose and pulls in no
// AppCompat dependency, so extending AppCompatActivity would fail to resolve. The manifest theme
// must match — @android:style/Theme.Material.Light.NoActionBar, not a Theme.AppCompat.* one.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate so the window is configured before the content view attaches.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
