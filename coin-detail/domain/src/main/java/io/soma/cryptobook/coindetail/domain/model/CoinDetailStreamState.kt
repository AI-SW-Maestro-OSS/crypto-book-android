package io.soma.cryptobook.coindetail.domain.model

sealed interface CoinDetailStreamState {
    data class Data(val value: CoinDetailVO) : CoinDetailStreamState
    data object Loading : CoinDetailStreamState
}
