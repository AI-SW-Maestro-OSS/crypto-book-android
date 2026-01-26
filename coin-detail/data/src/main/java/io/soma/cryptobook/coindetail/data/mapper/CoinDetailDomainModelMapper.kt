package io.soma.cryptobook.coindetail.data.mapper

import io.soma.cryptobook.coindetail.domain.model.CoinDetailVO
import io.soma.cryptobook.core.data.model.CoinTickerDto
import javax.inject.Inject

class CoinDetailDomainModelMapper @Inject constructor() {
    fun toDomainModel(coinTickerDto: CoinTickerDto): CoinDetailVO = CoinDetailVO(
        symbol = coinTickerDto.symbol,
        currentPrice = coinTickerDto.lastPrice.toBigDecimal(),
        priceChange = coinTickerDto.priceChange.toBigDecimal(),
        priceChangePercent = coinTickerDto.priceChangePercent.toDouble(),
        high24h = coinTickerDto.highPrice.toBigDecimal(),
        low24h = coinTickerDto.lowPrice.toBigDecimal(),
        volume24h = coinTickerDto.quoteAssetVolume.toBigDecimal(),
        openPrice = coinTickerDto.openPrice.toBigDecimal(),
    )
}
