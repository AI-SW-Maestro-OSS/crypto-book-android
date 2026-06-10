package io.soma.cryptobook.coindetail.presentation

import io.github.helpingstar.kandle.data.FinancialChartModelProducer
import io.soma.cryptobook.coindetail.presentation.component.ORDER_BOOK_ROW_COUNT
import io.soma.cryptobook.coindetail.presentation.component.OrderBookRowUiModel
import io.soma.cryptobook.core.designsystem.util.Text
import io.soma.cryptobook.core.presentation.UiState
import io.soma.cryptobook.core.presentation.mvi.UnidirectionalViewModel
import java.math.BigDecimal

interface CoinDetailContract {

    interface ViewModel : UnidirectionalViewModel<State, Event, Effect> {
        /**
         * Chart data pipe (vico/Kandle best practice, §Q13): the producer is held by the ViewModel
         * so it survives configuration changes and is fed incrementally off the candle stream.
         * Deliberately outside the [State] flow — chart series use a conflated, async channel.
         */
        val chartProducer: FinancialChartModelProducer
    }

    data class State(
        val symbol: String = "",
        val imageUrl: String = "",
        val currentPrice: String = "",
        val priceChangeText: String = "",
        val priceChangePercent: Double = 0.0,
        val high24h: String = "",
        val low24h: String = "",
        val volume24h: String = "",
        val openPrice: String = "",
        val tickSize: BigDecimal? = null,
        val orderBook: OrderBookUiModel = OrderBookUiModel.EMPTY,
        val isWatchlisted: Boolean = false,
        val isLoading: Boolean = true,
        val errorMsg: Text? = null,
        val realtimeStatusMessage: Text? = null,
    ) : UiState

    sealed interface Event {
        data object OnBackClicked : Event
        data object OnScreenStarted : Event
        data object OnFavoriteClicked : Event
    }

    sealed interface Effect {
        data object NavigateBack : Effect
    }
}

data class OrderBookUiModel(
    val rows: List<OrderBookRowUiModel>,
    val bidPercentText: String,
    val askPercentText: String,
    val bidRatio: Float,
    val tickSizeText: String,
) {
    companion object {
        val EMPTY = OrderBookUiModel(
            rows = List(ORDER_BOOK_ROW_COUNT) { OrderBookRowUiModel(bid = null, ask = null) },
            bidPercentText = "",
            askPercentText = "",
            bidRatio = 0.5f,
            tickSizeText = "",
        )
    }
}
