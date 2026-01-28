package io.soma.cryptobook.home.data.mapper

import io.soma.cryptobook.core.data.model.CoinTickerDto
import io.soma.cryptobook.core.domain.model.CoinPriceVO
import javax.inject.Inject

class CoinPriceDomainModelMapper @Inject constructor() {
    fun toDomainModel(coinTickerDto: CoinTickerDto): CoinPriceVO = CoinPriceVO(
        symbol = coinTickerDto.symbol,
        price = coinTickerDto.lastPrice.toBigDecimal(),
        priceChangePercentage24h = coinTickerDto.priceChangePercent.toDouble(),
    )
}
