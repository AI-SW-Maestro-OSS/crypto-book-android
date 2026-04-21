package io.soma.cryptobook.core.domain.repository

interface TickSizeRepository {
    suspend fun refreshTickSizesIfRequired(nowMillis: Long)
}
