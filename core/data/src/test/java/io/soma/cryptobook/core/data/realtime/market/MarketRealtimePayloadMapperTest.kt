package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.network.market.WsKlineEventPayload
import io.soma.cryptobook.core.network.market.WsKlinePayload
import io.soma.cryptobook.core.network.market.WsMiniTickerPayload
import io.soma.cryptobook.core.network.market.WsTickerPayload
import org.junit.Assert.assertEquals
import org.junit.Test

class MarketRealtimePayloadMapperTest {
    private val mapper = MarketRealtimePayloadMapper()

    @Test
    fun `mini ticker mapper calculates change and normalizes symbol`() {
        val dto = mapper.toTickerDto(
            WsMiniTickerPayload(
                symbol = "btcusdt",
                lastPrice = "110.0",
                openPrice = "100.0",
                highPrice = "111.0",
                lowPrice = "95.0",
                volume = "123.0",
                quoteAssetVolume = "456.0",
            ),
        )

        assertEquals("BTCUSDT", dto.symbol)
        assertEquals("10", dto.priceChange)
        assertEquals("10", dto.priceChangePercent)
    }

    @Test
    fun `ticker mapper preserves payload values and normalizes symbol`() {
        val dto = mapper.toTickerDto(
            WsTickerPayload(
                symbol = "ethusdt",
                lastPrice = "2000.0",
                priceChangePercent = "2.5",
                priceChange = "50.0",
                lowPrice = "1900.0",
                highPrice = "2050.0",
                quoteAssetVolume = "9999.0",
                openPrice = "1950.0",
            ),
        )

        assertEquals("ETHUSDT", dto.symbol)
        assertEquals("2.5", dto.priceChangePercent)
        assertEquals("50.0", dto.priceChange)
    }

    @Test
    fun `kline mapper normalizes symbol and interval`() {
        val dto = mapper.toKlineDto(
            WsKlineEventPayload(
                symbol = "xrpusdt",
                kline = WsKlinePayload(
                    openTime = 1L,
                    closeTime = 2L,
                    symbol = "xrpusdt",
                    interval = "1D",
                    openPrice = "1.0",
                    closePrice = "1.1",
                    highPrice = "1.2",
                    lowPrice = "0.9",
                    volume = "100.0",
                    isClosed = false,
                ),
            ),
        )

        assertEquals("XRPUSDT", dto.symbol)
        assertEquals("1d", dto.interval)
    }
}
