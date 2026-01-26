package io.soma.cryptobook.core.data.model

import io.soma.cryptobook.core.domain.model.CoinPriceVO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoinTickerDto(
    /** 심볼 (BTCUSDT) */
    @SerialName("s") val symbol: String,

    /** 현재가 */
    @SerialName("c") val lastPrice: String,

    /** 24시간 변동률 */
    @SerialName("P") val priceChangePercent: String,

    /** 24시간 변동 가격 */
    @SerialName("p") val priceChange: String,

    /** 24시간 최저가 */
    @SerialName("l") val lowPrice: String,

    /** 24시간 최고가 */
    @SerialName("h") val highPrice: String,

    /** 24시간 총 거래대금 */
    @SerialName("q") val quoteAssetVolume: String,

    /** 24시간 전 시가 */
    @SerialName("o") val openPrice: String,
)

fun CoinTickerDto.toCoinPriceVO() = CoinPriceVO(
    symbol = symbol,
    price = lastPrice.toBigDecimal(),
    priceChangePercentage24h = priceChangePercent.toDouble(),
)
