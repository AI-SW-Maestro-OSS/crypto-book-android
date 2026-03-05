package io.soma.cryptobook.core.network.session

data class WsSessionPolicy(
    val maxLifetimeMs: Long = 24 * 60 * 60 * 1000L,
    val rotateBeforeExpiryMs: Long = 10 * 60 * 1000L,
    val initialReconnectDelayMs: Long = 1_000L,
    val maxReconnectDelayMs: Long = 30_000L,
    val maxReconnectCount: Int = 5,
    val backoffMultiplier: Double = 2.0,
    val jitterRatio: Double = 0.2,
)
