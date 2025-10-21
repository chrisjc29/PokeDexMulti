package com.unomaster.test

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform