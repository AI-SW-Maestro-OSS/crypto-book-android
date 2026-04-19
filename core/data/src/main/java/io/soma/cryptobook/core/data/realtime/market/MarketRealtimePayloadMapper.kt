package io.soma.cryptobook.core.data.realtime.market

import io.soma.cryptobook.core.data.model.CoinKlineDto
import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.network.market.WsKlineEventPayload
import io.soma.cryptobook.core.network.market.WsMiniTickerPayload
import io.soma.cryptobook.core.network.market.WsTickerPayload
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRealtimePayloadMapper @Inject constructor() {
    fun toTickerDto(payload: WsMiniTickerPayload): CoinTickerDto {
        val openPriceDecimal = payload.openPrice.toBigDecimalOrNull()
        val lastPriceDecimal = payload.lastPrice.toBigDecimalOrNull()
        val priceChange = if (openPriceDecimal != null && lastPriceDecimal != null) {
            lastPriceDecimal.subtract(openPriceDecimal)
        } else {
            BigDecimal.ZERO
        }
        val priceChangePercent = if (
            openPriceDecimal == null ||
            lastPriceDecimal == null ||
            openPriceDecimal.compareTo(BigDecimal.ZERO) == 0
        ) {
            BigDecimal.ZERO
        } else {
            priceChange
                .multiply(BigDecimal("100"))
                .divide(openPriceDecimal, 8, RoundingMode.HALF_UP)
        }

        return CoinTickerDto(
            symbol = payload.symbol.uppercase(),
            lastPrice = payload.lastPrice,
            priceChangePercent = priceChangePercent.stripTrailingZeros().toPlainString(),
            priceChange = priceChange.stripTrailingZeros().toPlainString(),
            lowPrice = payload.lowPrice,
            highPrice = payload.highPrice,
            quoteAssetVolume = payload.quoteAssetVolume,
            openPrice = payload.openPrice,
        )
    }

    fun toTickerDto(payload: WsTickerPayload): CoinTickerDto = CoinTickerDto(
        symbol = payload.symbol.uppercase(),
        lastPrice = payload.lastPrice,
        priceChangePercent = payload.priceChangePercent,
        priceChange = payload.priceChange,
        lowPrice = payload.lowPrice,
        highPrice = payload.highPrice,
        quoteAssetVolume = payload.quoteAssetVolume,
        openPrice = payload.openPrice,
    )

    fun toKlineDto(payload: WsKlineEventPayload): CoinKlineDto = CoinKlineDto(
        symbol = payload.symbol.uppercase(),
        interval = payload.kline.interval.lowercase(),
        openTime = payload.kline.openTime,
        closeTime = payload.kline.closeTime,
        openPrice = payload.kline.openPrice,
        closePrice = payload.kline.closePrice,
        highPrice = payload.kline.highPrice,
        lowPrice = payload.kline.lowPrice,
        volume = payload.kline.volume,
        isClosed = payload.kline.isClosed,
    )
}
