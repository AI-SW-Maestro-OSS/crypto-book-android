package io.soma.cryptobook.coindetail.data.repository

import io.mockk.coEvery
import io.mockk.mockk
import io.soma.cryptobook.coindetail.data.datasource.CoinDetailKlineBackfillDataSource
import io.soma.cryptobook.coindetail.data.datasource.CoinDetailTickerSnapshotDataSource
import io.soma.cryptobook.coindetail.data.mapper.CoinDetailDomainModelMapper
import io.soma.cryptobook.coindetail.domain.model.CoinDetailStreamState
import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.data.realtime.kline.InMemoryWsKlineTable
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeCoordinator
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeDemandFatalSignal
import io.soma.cryptobook.core.data.realtime.market.MarketRealtimeRuntimeState
import io.soma.cryptobook.core.data.realtime.ticker.InMemoryWsTickerTable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinDetailRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `observeCoinDetail retains and releases symbol demand with collector lifecycle`() = runTest(
        testDispatcher,
    ) {
        val coordinator = FakeMarketRealtimeCoordinator()
        val repository = createRepository(
            coordinator = coordinator,
            tickerSnapshotDataSource = mockTickerSnapshotDataSource(),
            klineBackfillDataSource = mockKlineBackfillDataSource(),
        )

        val job = backgroundScope.launch(testDispatcher) {
            repository.observeCoinDetail("btcusdt").collect { }
        }

        advanceUntilIdle()
        assertEquals(listOf("BTCUSDT"), coordinator.retainedSymbols)

        job.cancel()
        advanceUntilIdle()
        assertEquals(listOf("BTCUSDT"), coordinator.releasedSymbols)
    }

    @Test
    fun `observeCoinDetail fails only on current symbol fatal signal`() = runTest(testDispatcher) {
        val coordinator = FakeMarketRealtimeCoordinator()
        val repository = createRepository(
            coordinator = coordinator,
            tickerSnapshotDataSource = mockTickerSnapshotDataSource(),
            klineBackfillDataSource = mockKlineBackfillDataSource(),
        )
        val expected = IllegalStateException("symbol fatal")

        val failure = backgroundScope.async(testDispatcher) {
            runCatching {
                repository.observeCoinDetail("BTCUSDT").collect { }
            }.exceptionOrNull()
        }

        advanceUntilIdle()
        coordinator.emitFatal(MarketRealtimeDemandFatalSignal.Symbol("BTCUSDT", expected))
        advanceUntilIdle()

        val actual = requireNotNull(failure.await())
        assertEquals(expected::class.java, actual::class.java)
        assertEquals(expected.message, actual.message)
    }

    @Test
    fun `observeCoinDetail ignores other symbol fatal signal and keeps emitting data`() = runTest(
        testDispatcher,
    ) {
        val coordinator = FakeMarketRealtimeCoordinator()
        val repository = createRepository(
            coordinator = coordinator,
            tickerSnapshotDataSource = mockTickerSnapshotDataSource(),
            klineBackfillDataSource = mockKlineBackfillDataSource(),
        )
        val emissions = mutableListOf<CoinDetailStreamState>()

        backgroundScope.launch(testDispatcher) {
            repository.observeCoinDetail("BTCUSDT")
                .take(3)
                .toList(emissions)
        }

        advanceUntilIdle()
        coordinator.emitFatal(
            MarketRealtimeDemandFatalSignal.Symbol(
                symbol = "ETHUSDT",
                cause = IllegalStateException("other symbol"),
            ),
        )
        coordinator.tickerTable.upsert(
            CoinTickerDto(
                symbol = "BTCUSDT",
                lastPrice = "101.5",
                priceChangePercent = "1.5",
                priceChange = "1.5",
                lowPrice = "99.0",
                highPrice = "102.0",
                quoteAssetVolume = "1000.0",
                openPrice = "100.0",
            ),
        )
        coordinator.klineTable.replace(
            symbol = "BTCUSDT",
            interval = "1d",
            candles = listOf(
                CoinKlineDto(
                    symbol = "BTCUSDT",
                    interval = "1d",
                    openTime = 1L,
                    closeTime = 2L,
                    openPrice = "100.0",
                    closePrice = "101.0",
                    highPrice = "102.0",
                    lowPrice = "99.0",
                    volume = "10.0",
                    isClosed = true,
                ),
            ),
        )

        advanceUntilIdle()

        assertEquals(3, emissions.size)
        val initial = emissions.first() as CoinDetailStreamState.Data
        assertEquals("BTCUSDT", initial.value.symbol)
        assertEquals("100.0", initial.value.currentPrice.toPlainString())
        val tickerUpdated = emissions[1] as CoinDetailStreamState.Data
        assertEquals("101.5", tickerUpdated.value.currentPrice.toPlainString())
        val candlesUpdated = emissions.last() as CoinDetailStreamState.Data
        assertEquals("BTCUSDT", candlesUpdated.value.symbol)
        assertEquals("101.5", candlesUpdated.value.currentPrice.toPlainString())
        assertEquals(1, candlesUpdated.candles.size)
    }

    @Test
    fun `observeCoinDetail refreshes bootstrap again when runtime reconnects`() = runTest(
        testDispatcher,
    ) {
        val coordinator = FakeMarketRealtimeCoordinator()
        var tickerSnapshotCalls = 0
        var klineBackfillCalls = 0
        val repository = createRepository(
            coordinator = coordinator,
            tickerSnapshotDataSource = mockTickerSnapshotDataSource {
                tickerSnapshotCalls += 1
            },
            klineBackfillDataSource = mockKlineBackfillDataSource {
                klineBackfillCalls += 1
            },
        )

        val job = backgroundScope.launch(testDispatcher) {
            repository.observeCoinDetail("BTCUSDT").collect { }
        }

        advanceUntilIdle()
        assertEquals(1, tickerSnapshotCalls)
        assertEquals(1, klineBackfillCalls)

        coordinator.runtimeStateFlow.value = coordinator.runtimeStateFlow.value.copy(isConnected = true)
        advanceUntilIdle()

        assertEquals(2, tickerSnapshotCalls)
        assertEquals(2, klineBackfillCalls)

        job.cancel()
    }

    private fun createRepository(
        coordinator: FakeMarketRealtimeCoordinator,
        tickerSnapshotDataSource: CoinDetailTickerSnapshotDataSource,
        klineBackfillDataSource: CoinDetailKlineBackfillDataSource,
    ): CoinDetailRepositoryImpl = CoinDetailRepositoryImpl(
        marketRealtimeCoordinator = coordinator,
        tickerSnapshotDataSource = tickerSnapshotDataSource,
        klineBackfillDataSource = klineBackfillDataSource,
        tickerTable = coordinator.tickerTable,
        klineTable = coordinator.klineTable,
        coinDetailDomainModelMapper = CoinDetailDomainModelMapper(),
        ioDispatcher = testDispatcher,
    )

    private fun mockTickerSnapshotDataSource(
        onCall: (() -> Unit)? = null,
    ): CoinDetailTickerSnapshotDataSource {
        val dataSource = mockk<CoinDetailTickerSnapshotDataSource>()
        coEvery { dataSource.getTicker("BTCUSDT") } answers {
            onCall?.invoke()
            CoinTickerDto(
                symbol = "BTCUSDT",
                lastPrice = "100.0",
                priceChangePercent = "1.0",
                priceChange = "1.0",
                lowPrice = "99.0",
                highPrice = "101.0",
                quoteAssetVolume = "1000.0",
                openPrice = "99.0",
            )
        }
        return dataSource
    }

    private fun mockKlineBackfillDataSource(
        onCall: (() -> Unit)? = null,
    ): CoinDetailKlineBackfillDataSource {
        val dataSource = mockk<CoinDetailKlineBackfillDataSource>()
        coEvery {
            dataSource.getAllKlines(
                symbol = "BTCUSDT",
                interval = "1d",
                pageLimit = 1000,
            )
        } answers {
            onCall?.invoke()
            emptyList()
        }
        return dataSource
    }

    private class FakeMarketRealtimeCoordinator : MarketRealtimeCoordinator {
        val tickerTable = InMemoryWsTickerTable()
        val klineTable = InMemoryWsKlineTable()
        val runtimeStateFlow = MutableStateFlow(MarketRealtimeRuntimeState.initial(nowMillis = 0L))
        private val fatalSignals = MutableSharedFlow<MarketRealtimeDemandFatalSignal>(
            extraBufferCapacity = 16,
        )

        val retainedSymbols = mutableListOf<String>()
        val releasedSymbols = mutableListOf<String>()

        override val runtimeState: StateFlow<MarketRealtimeRuntimeState> = runtimeStateFlow
        override val demandFatalSignals: SharedFlow<MarketRealtimeDemandFatalSignal> = fatalSignals

        override fun start() = Unit

        override fun stop() = Unit

        override suspend fun retainSymbol(symbol: String) {
            retainedSymbols += symbol
        }

        override suspend fun releaseSymbol(symbol: String) {
            releasedSymbols += symbol
        }

        fun emitFatal(signal: MarketRealtimeDemandFatalSignal) {
            fatalSignals.tryEmit(signal)
        }
    }
}
