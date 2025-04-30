package br.com.omnidevs.gcsn

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform