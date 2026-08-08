package com.unomaster.pokedexgame.fake

import com.unomaster.pokedexgame.data.local.KeyValueStore

// An in-memory store. initial seeds it so a test can start from "the user already chose this".
class FakeKeyValueStore(
    initial: Map<String, Any> = emptyMap(),
) : KeyValueStore {

    private val values = initial.toMutableMap()

    override fun getString(key: String): String? = values[key] as? String

    override fun putString(key: String, value: String) {
        values[key] = value
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        values[key] as? Boolean ?: default

    override fun putBoolean(key: String, value: Boolean) {
        values[key] = value
    }

    override fun remove(key: String) {
        values.remove(key)
    }
}
