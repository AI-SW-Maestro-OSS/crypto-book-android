package io.soma.cryptobook.home.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.data.realtime.ticker.InMemoryWsTickerTable
import io.soma.cryptobook.home.data.datasource.CoinListRemoteDataSource
import io.soma.cryptobook.home.data.datasource.CoinListStreamDataSource
import io.soma.cryptobook.home.data.mapper.CoinPriceDomainModelMapper
import io.soma.cryptobook.home.data.model.BinanceTickerDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CoinRepositoryImplTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `observeCoinPrices propagates ticker table updates after initial rest snapshot`() = runTest(testDispatcher) {
        val remoteDataSource = mockk<CoinListRemoteDataSource>()
        val streamDataSource = mockk<CoinListStreamDataSource>()
        val tickerTable = InMemoryWsTickerTable()
        val repository = CoinRepositoryImpl(
            coinListRemoteDataSource = remoteDataSource,
            coinListStreamDataSource = streamDataSource,
            tickerTable = tickerTable,
            coinPriceDomainModelMapper = CoinPriceDomainModelMapper(),
            ioDispatcher = testDispatcher,
        )

        coEvery { remoteDataSource.getAllTickerPrices() } returns listOf(
            BinanceTickerDto(
                symbol = "BTCUSDT",
                lastPrice = "100.0",
                priceChangePercent = "1.0",
            ),
            BinanceTickerDto(
                symbol = "ETHUSDT",
                lastPrice = "200.0",
                priceChangePercent = "2.0",
            ),
        )
        every { streamDataSource.maintainCoinListStream() } returns emptyFlow()

        val emissions = mutableListOf<List<io.soma.cryptobook.core.domain.model.CoinPriceVO>>()
        backgroundScope.launch(testDispatcher) {
            repository.observeCoinPrices()
                .take(2)
                .toList(emissions)
        }

        tickerTable.upsertAll(
            listOf(
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
            ),
        )

        advanceUntilIdle()

        assertEquals(2, emissions.size)
        assertEquals("100.0", emissions.first().first { it.symbol == "BTCUSDT" }.price.toPlainString())
        assertEquals("101.5", emissions.last().first { it.symbol == "BTCUSDT" }.price.toPlainString())
        assertEquals("200.0", emissions.last().first { it.symbol == "ETHUSDT" }.price.toPlainString())
    }
}
