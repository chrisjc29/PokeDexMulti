package com.unomaster.pokedexgame.data.local

// The persistence seam. Common code stores preferences through this; each platform provides the
// implementation (SharedPreferences on Android, NSUserDefaults on iOS) in platformModule.
// Deliberately narrow — widen it when a real need appears, not in anticipation of one.
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
}
