package com.unomaster.pokedexgame.time

import com.unomaster.pokedexgame.domain.time.CurrentTimeProvider
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IosCurrentTimeProvider : CurrentTimeProvider {
    override fun currentEpochSeconds(): Long = NSDate().timeIntervalSince1970.toLong()
}
