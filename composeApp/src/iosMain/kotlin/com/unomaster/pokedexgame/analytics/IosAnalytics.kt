package com.unomaster.pokedexgame.analytics

class IosAnalytics : Analytics {
    override fun logEvent(name: String, params: Map<String, String>) {
        IosFirebaseBridges.analytics?.logEvent(name, params)
    }

    override fun setUserId(id: String?) {
        IosFirebaseBridges.analytics?.setUserId(id)
    }
}
