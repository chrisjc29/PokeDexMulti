package com.unomaster.pokedexgame.data.local

import platform.Foundation.NSUserDefaults

class IosKeyValueStore : KeyValueStore {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    // NSUserDefaults returns false for an absent key, so an explicit presence check is needed for
    // the default to mean anything.
    override fun getBoolean(key: String, default: Boolean): Boolean =
        if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)

    override fun putBoolean(key: String, value: Boolean) {
        defaults.setBool(value, key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
