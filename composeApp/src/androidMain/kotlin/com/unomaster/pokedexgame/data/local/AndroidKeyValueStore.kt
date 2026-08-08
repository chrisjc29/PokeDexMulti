package com.unomaster.pokedexgame.data.local

import android.content.Context
import android.content.SharedPreferences

// SharedPreferences rather than DataStore: the store is synchronous by contract, and adding a
// coroutine-based API here would push suspend functions into every settings read for no benefit at
// this size. Swap the implementation if the app grows to need flows — the interface doesn't change.
class AndroidKeyValueStore(
    context: Context,
) : KeyValueStore {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("com.unomaster.pokedexgame.preferences", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        preferences.getBoolean(key, default)

    override fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    override fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
}
