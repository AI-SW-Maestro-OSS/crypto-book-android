package io.soma.cryptobook.core.data.repository

import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeDao
import io.soma.cryptobook.core.data.database.ticksize.SymbolTickSizeEntity
import io.soma.cryptobook.core.data.datasource.ticksize.TickSizeRemoteDataSource
import io.soma.cryptobook.core.data.datastore.CbPreferencesDataSource
import io.soma.cryptobook.core.domain.repository.TickSizeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TickSizeRepositoryImpl @Inject constructor(
    private val remoteDataSource: TickSizeRemoteDataSource,
    private val tickSizeDao: SymbolTickSizeDao,
    private val preferencesDataSource: CbPreferencesDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : TickSizeRepository {
    override suspend fun refreshTickSizesIfRequired(nowMillis: Long) = withContext(ioDispatcher) {
        val lastCheckedAtMillis = preferencesDataSource.lastTickSizeCheckedAtMillis.first()
        val elapsedMillis = nowMillis - lastCheckedAtMillis
        if (
            lastCheckedAtMillis > 0L &&
            elapsedMillis in 0 until TICK_SIZE_REFRESH_INTERVAL_MILLIS
        ) {
            return@withContext
        }

        val tickSizes = remoteDataSource.getExchangeInfo()
            .symbols
            .mapNotNull { symbolInfo ->
                val tickSize = symbolInfo.filters
                    .firstOrNull { it.filterType == PRICE_FILTER }
                    ?.tickSize
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null

                SymbolTickSizeEntity(
                    symbol = symbolInfo.symbol.uppercase(),
                    tickSize = tickSize,
                )
            }
            .distinctBy { it.symbol }

        tickSizeDao.replaceAll(tickSizes)
        preferencesDataSource.setLastTickSizeCheckedAtMillis(nowMillis)
    }

    private companion object {
        private const val PRICE_FILTER = "PRICE_FILTER"
        private const val TICK_SIZE_REFRESH_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L
    }
}
