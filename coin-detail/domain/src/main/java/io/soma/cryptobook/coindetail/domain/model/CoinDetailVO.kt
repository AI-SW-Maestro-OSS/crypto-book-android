package io.soma.cryptobook.coindetail.domain.model

import java.math.BigDecimal

/**
 * 코인 상세 정보 (Value Object)
 *
 * 24시간 ticker 데이터를 포함하는 domain model
 *
 * @property symbol 심볼 (e.g., "BTCUSDT")
 * @property currentPrice 현재가
 * @property priceChange 24시간 변동액 (절대값)
 * @property priceChangePercent 24시간 변동률 (백분율)
 * @property high24h 24시간 최고가
 * @property low24h 24시간 최저가
 * @property volume24h 24시간 총 거래대금 (USDT)
 * @property openPrice 24시간 전 시가
 */
data class CoinDetailVO(
    val symbol: String,
    val currentPrice: BigDecimal,
    val priceChange: BigDecimal,
    val priceChangePercent: Double,
    val high24h: BigDecimal,
    val low24h: BigDecimal,
    val volume24h: BigDecimal,
    val openPrice: BigDecimal,
)
