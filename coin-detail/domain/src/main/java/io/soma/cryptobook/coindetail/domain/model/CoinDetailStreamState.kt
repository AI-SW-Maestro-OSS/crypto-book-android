package io.soma.cryptobook.coindetail.domain.model

sealed interface CoinDetailStreamState {
    data class Data(
        val value: CoinDetailVO,
        val candles: List<CoinCandleVO>,
        val orderBook: OrderBookVO?,
    ) : CoinDetailStreamState

    data object Loading : CoinDetailStreamState
}
