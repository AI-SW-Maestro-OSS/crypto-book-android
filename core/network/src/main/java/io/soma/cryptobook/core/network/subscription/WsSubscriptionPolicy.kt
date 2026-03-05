package io.soma.cryptobook.core.network.subscription

data class WsSubscriptionPolicy(
    val requestTimeoutMs: Long = 5_000L,
    val maxRequestRetry: Int = 3,
    val retryBackoffMs: List<Long> = listOf(500L, 1_000L, 2_000L),
) {
    fun backoffForAttempt(attempt: Int): Long {
        val index = (attempt - 1).coerceAtLeast(0)
        val fallback = retryBackoffMs.lastOrNull() ?: 0L
        return retryBackoffMs.getOrElse(index) { fallback }
    }
}
